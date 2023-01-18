/**
 * Copyright (C) 2006-2021 Talend Inc. - www.talend.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.talend.designer.core.ui.editor.properties.controllers.dynamic;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.talend.core.model.components.ComponentCategory;
import org.talend.core.model.components.IComponent;
import org.talend.core.model.general.ModuleNeeded;
import org.talend.core.model.metadata.IMetadataColumn;
import org.talend.core.model.metadata.IMetadataTable;
import org.talend.core.model.metadata.MetadataColumn;
import org.talend.core.model.metadata.MetadataTable;
import org.talend.core.model.process.EConnectionType;
import org.talend.core.model.process.IConnection;
import org.talend.core.model.process.IContext;
import org.talend.core.model.process.INode;
import org.talend.core.model.process.IProcess;
import org.talend.core.model.properties.Property;
import org.talend.core.ui.component.ComponentsFactoryProvider;
import org.talend.designer.core.i18n.Messages;
import org.talend.designer.core.model.process.DataConnection;
import org.talend.designer.core.model.process.DataProcess;
import org.talend.designer.core.ui.editor.nodecontainer.NodeContainer;
import org.talend.designer.core.ui.editor.nodes.Node;
import org.talend.designer.core.ui.editor.process.Process;
import org.talend.designer.core.ui.editor.properties.controllers.AbstractGuessSchemaProcess;
import org.talend.designer.runprocess.IProcessor;
import org.talend.designer.runprocess.ProcessorUtilities;

public class DynamicGuessSchemaProcess {

    private final Task guessSchemaTask;

    private final ExecutorService executorService;

    public DynamicGuessSchemaProcess(final INode node, final IContext context, final ExecutorService executorService) {
        this.executorService = executorService;
        this.guessSchemaTask = new Task(context, node, executorService);
    }

    public Future<GuessSchemaResult> run() {
        return executorService.submit(guessSchemaTask);
    }

    public void kill() {
        guessSchemaTask.kill();
    }

    public static class Task implements Callable<GuessSchemaResult> {

        private Process process;

        private final IContext context;

        private INode node;

        private final ExecutorService executorService;

        private java.lang.Process executeProcess;

        public Task(final IContext context, final INode node, final ExecutorService executorService) {
            this.context = context;
            this.node = node;
            this.executorService = executorService;
        }

        @Override
        public GuessSchemaResult call() throws Exception {
            buildProcess();
            IProcessor processor = ProcessorUtilities.getProcessor(process, null);
            processor.setContext(context);
            final String debug = System.getProperty("dynamic.guessschema.debug", null);
            executeProcess = processor.run(debug == null || debug.isEmpty() ? null : singletonList(debug).toArray(new String[0]),
                    IProcessor.NO_STATISTICS, IProcessor.NO_TRACES);

            final Future<GuessSchemaResult> result = executorService.submit(() -> {
                final Pattern pattern = Pattern.compile("^\\[\\s*(INFO|WARN|ERROR|DEBUG|TRACE)\\s*]");
                String out;
                final List<String> err = new ArrayList<>();
                // read stderr stream
                try (final BufferedReader reader = new BufferedReader(new InputStreamReader(executeProcess.getErrorStream()))) {
                    err.addAll(reader.lines().collect(toList()));
                    err.add("===== Root cause ======");
                }
                // read stdout stream
                try (final BufferedReader reader = new BufferedReader(new InputStreamReader(executeProcess.getInputStream()))) {
                    out = reader.lines().peek(l -> err.add(l)) // may have interesting infos during execution, adding to
                                                               // stack
                            .filter(l -> !pattern.matcher(l).find()) // filter out logs
                            .filter(l -> l.startsWith("[") || l.startsWith("{")) // ignore line with non json data
                            .collect(joining("\n"));
                }
                return new GuessSchemaResult(out, err.stream().collect(joining("\n")));
            });

            executeProcess.waitFor();
            final GuessSchemaResult guessResult = result.get();
            if (executeProcess.exitValue() != 0) {
                return new GuessSchemaResult(guessResult.getError(), guessResult.getError());
            }
            final String resultStr = guessResult.getResult();
            if (resultStr != null && !resultStr.trim().isEmpty()) {
                return guessResult;
            }
            final String errMessage = guessResult.getError();
            if (errMessage != null && !errMessage.isEmpty()) {
                throw new IllegalStateException(errMessage);
            } else {
                throw new IllegalStateException(Messages.getString("guessSchema.error.empty"));
            }
        }

        public synchronized void kill() {
            if (executeProcess != null && executeProcess.isAlive()) {
                final java.lang.Process p = executeProcess.destroyForcibly();
                try {
                    p.waitFor(20, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void buildProcess() {
            IProcess originalProcess;
            Property property = AbstractGuessSchemaProcess.getNewmockProperty();
            originalProcess = new Process(property);

            DataProcess dataProcess = new DataProcess(originalProcess);
            dataProcess.buildFromGraphicalProcess(Arrays.asList(node));
            process = Process.class.cast(dataProcess.getDuplicatedProcess());
            process.setGeneratingProcess(null);
            process.getContextManager().getListContext().addAll(originalProcess.getContextManager().getListContext());
            process.getContextManager().setDefaultContext(this.context);
            INode inputNode = process.getGraphicalNodes().stream().filter(n -> node.getUniqueName().equals(n.getUniqueName()))
                    .findFirst().orElse(null);
            IComponent guessComponent = ComponentsFactoryProvider.getInstance().getComponents().stream()
                    .filter(comp -> "tDynamicGuessSchema".equals(comp.getName())).findFirst().orElse(null);
            Node guessNode = new Node(guessComponent, process);
            process.addNodeContainer(new NodeContainer(guessNode));

            for (INode node : new ArrayList<>(process.getGraphicalNodes())) {
                for (ModuleNeeded module : node.getModulesNeeded()) {
                    if (module.isRequired(node.getElementParameters())) {
                        Node libNode = new Node(ComponentsFactoryProvider.getInstance().get("tLibraryLoad",
                                ComponentCategory.CATEGORY_4_DI.getName()), process);
                        libNode.setPropertyValue("LIBRARY", StringUtils.wrap(module.getMavenUri(), "\""));
                        NodeContainer nc = process.loadNodeContainer(libNode, false);
                        process.addNodeContainer(nc);
                    }
                }
            }

            List<IMetadataTable> tables = new ArrayList<>();
            inputNode.setMetadataList(tables);
            IMetadataTable table = new MetadataTable();
            table.setAttachedConnector(EConnectionType.FLOW_MAIN.getName());
            tables.add(table);
            List<IMetadataColumn> columns = new ArrayList<>();
            table.setListColumns(columns);
            IMetadataColumn column = new MetadataColumn();
            columns.add(column);
            column.setTalendType("id_Dynamic");
            column.setLabel("dynamic");
            column.setType("STRING");
            column.setPattern("dd-MM-yyyy");
            column.setLength(-1);
            column.setPrecision(-1);

            DataConnection connection = new DataConnection();
            connection.setActivate(true);
            connection.setLineStyle(EConnectionType.FLOW_MAIN);
            connection.setConnectorName(EConnectionType.FLOW_MAIN.getName());
            connection.setName("row1");
            connection.setSource(inputNode);
            connection.setTarget(guessNode);
            connection.setMetadataTable(table);
            List<IConnection> connections = new ArrayList<>();
            connections.add(connection);

            inputNode.setOutgoingConnections(connections);
            guessNode.setIncomingConnections(connections);
        }

    }

    public static class GuessSchemaResult {

        private String result;

        private String error;

        public GuessSchemaResult(String result, String error) {
            this.result = result;
            this.error = error;
        }

        public String getResult() {
            return result;
        }

        public void setResult(String result) {
            this.result = result;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

    }

}
