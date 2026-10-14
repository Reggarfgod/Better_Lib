package com.reggarf.mods.better_lib.configs.gui.element;

import com.reggarf.mods.better_lib.configs.utility.Color;
import com.reggarf.mods.better_lib.configs.utility.Couple;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class BoxElement extends RenderElement {

	protected Color background = new Color(0xff000000, true);
	protected Color borderTop = new Color(0x40ffeedd, true);
	protected Color borderBot = new Color(0x20ffeedd, true);
	protected int borderOffset = 2;

	public <T extends BoxElement> T withBackground(Color color) {
		this.background = color;
		return (T) this;
	}

	public <T extends BoxElement> T withBackground(int color) {
		return withBackground(new Color(color, true));
	}

	public <T extends BoxElement> T flatBorder(Color color) { this.borderTop = color; this.borderBot = color; return (T) this; }

	public <T extends BoxElement> T flatBorder(int color) { return flatBorder(new Color(color, true)); }

	public <T extends BoxElement> T withBorderColors(Couple<Color> colors) {
		this.borderTop = colors.getFirst();
		this.borderBot = colors.getSecond();
		return (T) this;
	}

	public <T extends BoxElement> T withBorderColors(Color top, Color bot) {
		this.borderTop = top;
		this.borderBot = bot;
		return (T) this;
	}

	public <T extends BoxElement> T gradientBorder(Couple<Color> colors) {
		this.borderTop = colors.getFirst();
		this.borderBot = colors.getSecond();
		return (T) this;
	}

	public <T extends BoxElement> T gradientBorder(Color top, Color bot) {
		this.borderTop = top;
		this.borderBot = bot;
		return (T) this;
	}

	public <T extends BoxElement> T withBorderOffset(int offset) {
		this.borderOffset = offset;
		return (T) this;
	}

	public Color getBackground() {
		return background;
	}

	@Override
	public void render(GuiGraphicsExtractor graphics) {
		int f = borderOffset;
		int x0 = (int) (x - f);
		int y0 = (int) (y - f);
		int x1 = (int) (x + width + f);
		int y1 = (int) (y + height + f);

		int bg = background.copy().scaleAlpha(alpha).getRGB();
		graphics.fill(x0, y0, x1, y1, bg);

		int topC = borderTop.copy().scaleAlpha(alpha).getRGB();
		int botC = borderBot.copy().scaleAlpha(alpha).getRGB();

		// Top border
		graphics.fill(x0 - 1, y0 - 1, x1 + 1, y0, topC);
		// Bottom border
		graphics.fill(x0 - 1, y1, x1 + 1, y1 + 1, botC);
		// Left border
		graphics.fillGradient(x0 - 1, y0, x0, y1, topC, botC);
		// Right border
		graphics.fillGradient(x1, y0, x1 + 1, y1, topC, botC);
	}
}
