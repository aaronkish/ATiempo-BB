/**
 * 
 */
package canive;

import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.LabelField;

/**
 * @author Abel
 *
 */
public class FCLabelField extends LabelField {

	public FCLabelField(Object text, long style) {
        super(text, style);
    }

    private int mFontColor = -1;

    public void setFontColor(int fontColor) {
        mFontColor = fontColor;
    }

    protected void paint(Graphics graphics) {
        if (-1 != mFontColor)
            graphics.setColor(mFontColor);
        super.paint(graphics);
    }

}
