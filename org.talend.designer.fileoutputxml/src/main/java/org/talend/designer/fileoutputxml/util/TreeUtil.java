// ============================================================================
//
// Copyright (C) 2006-2009 Talend Inc. - www.talend.com
//
// This source code is available under agreement available at
// %InstallDIR%\features\org.talend.rcp.branding.%PRODUCTNAME%\%PRODUCTNAME%license.txt
//
// You should have received a copy of the agreement
// along with this program; if not, write to Talend SA
// 9 rue Pages 92150 Suresnes, France
//
// ============================================================================
package org.talend.designer.fileoutputxml.util;

import org.talend.designer.fileoutputxml.data.Attribute;
import org.talend.designer.fileoutputxml.data.Element;
import org.talend.designer.fileoutputxml.data.FOXTreeNode;

/**
 * DOC ke class global comment. Detailled comment <br/>
 * 
 */
public class TreeUtil {

    /**
     * DOC ke Comment method "checkLoopNode".
     * 
     * @param node
     * @return
     */
    public static boolean checkTreeLoopNode(FOXTreeNode node) {
        if (node == null) {
            return false;
        }

        if (node instanceof Attribute) {
            return false;
        }

        Element e = (Element) node;

        if (e.getParent() == null) {
            return false;
        }

        if (e.isGroup()) {
            return true;
        }

        return true;

    }

    /**
     * DOC ke Comment method "guessAndSetLoopNode".
     * 
     * @param root
     */
    public static FOXTreeNode guessAndSetLoopNode(FOXTreeNode root) {
        FOXTreeNode loopNode = root;
        Element node = (Element) loopNode;
        if (node.getElementChildren().size() > 1) {
            node.setLoop(true);
            return node;
        }
        for (FOXTreeNode att : node.getAttributeChildren()) {
            if (att.getColumn() != null) {
                node.setLoop(true);
                return node;
            }
        }
        if (node.getElementChildren().size() == 0) {
            return null;
        }
        do {
            node = (Element) node.getElementChildren().get(0);
            if (node.getColumn() != null) {
                node.setLoop(true);
                return node;
            }
            if (node.getElementChildren().size() > 1) {
                node.setLoop(true);
                return node;
            }
            for (FOXTreeNode att : node.getAttributeChildren()) {
                if (att.getColumn() != null) {
                    node.setLoop(true);
                    return node;
                }
            }
            if (node.getElementChildren().size() == 0) {
                return null;
            }
        } while (true);
    }

    /**
     * DOC ke Comment method "setAsLoopNode".
     * 
     * @param node
     */
    public static void setAsLoopNode(FOXTreeNode node) {
        if (checkTreeLoopNode(node)) {
            node.setLoop(true);
        }
    }

    /**
     * DOC ke Comment method "clearLoopNode".
     * 
     * @param root
     */
    public static void clearLoopNode(FOXTreeNode root) {
        if (root instanceof Element) {
            Element e = (Element) root;
            if (e.isLoop()) {
                e.setLoop(false);
            } else {
                for (FOXTreeNode child : e.getElementChildren()) {
                    clearLoopNode(child);
                }
            }
        }
    }

    /**
     * DOC xzhang Comment method "clearMainNode".
     * 
     * @param root
     */
    public static void clearMainNode(FOXTreeNode root) {
        if (root instanceof Element) {
            Element e = (Element) root;
            if (e.isMain()) {
                e.setMain(false);
            }
            for (FOXTreeNode child : e.getElementChildren()) {
                clearMainNode(child);
            }
        }
    }

    /**
     * DOC xzhang Comment method "clearMainNode".
     * 
     * @param root
     */
    public static void upsetMainNode(FOXTreeNode loop) {
        if (loop instanceof Element) {
            FOXTreeNode parent = loop;
            while (parent != null) {
                parent.setMain(true);
                parent = parent.getParent();
            }

        }
    }

    /**
     * DOC ke Comment method "getLoopNode".
     * 
     * @param root
     * @return
     */
    public static FOXTreeNode getLoopNode(FOXTreeNode root) {
        if (root != null && root instanceof Element) {
            Element e = (Element) root;
            if (e.isLoop()) {
                return e;
            }
            for (FOXTreeNode child : e.getElementChildren()) {
                FOXTreeNode loopNode = getLoopNode(child);
                if (loopNode != null) {
                    return loopNode;
                }
            }
        }
        return null;
    }

    /**
     * DOC ke Comment method "getGroupNode".
     * 
     * @param root
     * @return
     */
    public static FOXTreeNode getGroupNode(FOXTreeNode root) {
        if (root != null && root instanceof Element) {
            Element e = (Element) root;
            if (e.isLoop()) {
                return null;
            }
            if (e.isGroup()) {
                return e;
            }
            for (FOXTreeNode child : e.getElementChildren()) {
                FOXTreeNode loopNode = getGroupNode(child);
                if (loopNode != null) {
                    return loopNode;
                }
            }
        }
        return null;
    }

    /**
     * DOC ke Comment method "betweenGroupAndLoop".
     * 
     * @param node
     * @return whether the currnt node is between the group node and loop node in the tree hierachy.
     */
    public static boolean isSubLoopNode(FOXTreeNode node) {
        if (node instanceof Attribute || node.isGroup() || node.isLoop()) {
            return false;
        }
        boolean flag = false;
        FOXTreeNode temp = node.getParent();
        while (temp != null) {
            if (temp.isGroup()) {
                flag = true;
                break;
            }
            if (temp.isLoop()) {
                return false;
            }
            temp = temp.getParent();
        }
        if (flag) {
            if (TreeUtil.findDownLoopNode(node) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * DOC ke Comment method "clearSubGroupNode".
     * 
     * @param node
     */
    public static void clearSubGroupNode(FOXTreeNode node) {
        if (node instanceof Attribute) {
            return;
        }
        if (node.isGroup()) {
            node.setGroup(false);
            return;
        }
        if (node.isLoop()) {
            return;
        }
        for (FOXTreeNode child : node.getChildren()) {
            clearSubGroupNode(child);
        }
    }

    /**
     * DOC ke Comment method "guessLoopWithGroup".
     * 
     * @param node
     * @return
     */
    public static boolean guessLoopWithGroup(FOXTreeNode node) {
        if (node instanceof Attribute) {
            return false;
        }
        boolean result = false;
        if (!node.isGroup()) {
            FOXTreeNode tNode = node;
            while (true) {
                if (tNode.isGroup()) {
                    node = tNode;
                    result = true;
                    break;
                }
                if (((Element) tNode).getElementChildren().size() == 0) {
                    break;
                }
                tNode = ((Element) tNode).getElementChildren().get(0);
            }
        } else {
            result = true;
        }
        if (!result) {
            return false;
        }
        Element e = (Element) node;
        do {
            if (e.getElementChildren().size() > 1 || e.hasLink()) {
                e.setLoop(true);
                return true;
            }
            if (e.getElementChildren().size() == 0) {
                return true;
            }
            e = (Element) e.getElementChildren().get(0);
        } while (true);
    }

    /**
     * DOC ke Comment method "checkTreeGoupNode".
     * 
     * @param node
     * @return
     */
    public static boolean checkTreeGoupNode(FOXTreeNode node) {
        if (node == null) {
            return false;
        }

        if (node instanceof Attribute) {
            return false;
        }

        if (node.getParent() == null) {
            return false;
        }

        FOXTreeNode loop = findDownLoopNode(node);
        if (loop == null || loop == node) {
            return false;
        }
        return true;
    }

    /*
     * find the loop node in "treenode" and its children
     */
    private static FOXTreeNode findDownLoopNode(FOXTreeNode treeNode) {
        if (treeNode instanceof Element) {
            Element e = (Element) treeNode;
            if (e.isLoop()) {
                return e;
            }
            for (FOXTreeNode child : e.getChildren()) {
                FOXTreeNode tmp = findDownLoopNode(child);
                if (tmp != null) {
                    return tmp;
                }
            }
        }
        return null;
    }

    public static FOXTreeNode findUpGroupNode(FOXTreeNode treeNode) {
        if (treeNode != null) {
            if (treeNode.isGroup()) {
                return treeNode;
            }
            if (treeNode instanceof Element) {
                return findUpGroupNode(treeNode.getParent());
            }
        }
        return null;
    }

    public static String getPath(FOXTreeNode treeNode) {
        StringBuffer path = new StringBuffer();
        FOXTreeNode tmp = treeNode;
        while (tmp != null) {
            path.insert(0, "/" + tmp.getLabel()); //$NON-NLS-1$
            tmp = tmp.getParent();
        }
        return path.toString();
    }
}
