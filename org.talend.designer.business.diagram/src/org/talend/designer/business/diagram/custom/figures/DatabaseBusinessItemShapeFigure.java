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
package org.talend.designer.business.diagram.custom.figures;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * DOC mhelleboid class global comment. Detailled comment <br/>
 * 
 * $Id$
 * 
 */
public class DatabaseBusinessItemShapeFigure extends BusinessItemShapeFigure {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.gmf.runtime.gef.ui.figures.NodeFigure#paintFigure(org.eclipse.draw2d.Graphics)
     */
    @Override
    protected void paintFigure(Graphics g) {
        Rectangle r = getInnerBounds();

        int ellipseHeight = (int) (r.height * 0.25);

        Rectangle ellipse = new Rectangle(r.x, r.y, r.width, ellipseHeight);
        Rectangle middle = new Rectangle(r.x, r.y + (ellipseHeight / 2), r.width, r.height - ellipseHeight);
        Rectangle lowerArc = new Rectangle(r.x, r.y + r.height - ellipseHeight, r.width, ellipseHeight);

        g.fillOval(ellipse);
        g.fillRectangle(middle);
        g.fillArc(lowerArc, 180, 180);

        g.drawOval(ellipse.x, ellipse.y, ellipse.width, ellipse.height);
        g.drawLine(middle.x, middle.y, middle.x, middle.y + middle.height);
        g.drawLine(middle.x + middle.width, middle.y, middle.x + middle.width, middle.y + middle.height);
        g.drawArc(lowerArc, 180, 180);
    }

}
