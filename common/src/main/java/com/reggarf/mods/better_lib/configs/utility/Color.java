package com.reggarf.mods.better_lib.configs.utility;

import java.util.function.UnaryOperator;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

public class Color {
	public final static Color TRANSPARENT_BLACK = new Color(0, 0, 0, 0).setImmutable();
	public final static Color BLACK = new Color(0, 0, 0).setImmutable();
	public final static Color WHITE = new Color(255, 255, 255).setImmutable();
	public final static Color RED = new Color(255, 0, 0).setImmutable();
	public final static Color GREEN = new Color(0, 255, 0).setImmutable();
	public final static Color SPRING_GREEN = new Color(0, 255, 187).setImmutable();

	protected boolean mutable = true;
	protected int value;

	public Color(int r, int g, int b) {
		this(r, g, b, 0xff);
	}

	public Color(int r, int g, int b, int a) {
		value = ((a & 0xff) << 24) |
				((r & 0xff) << 16) |
				((g & 0xff) << 8)  |
				((b & 0xff) << 0);
	}

	public Color(float r, float g, float b, float a) {
		this(
				(int) (0.5 + 0xff * Mth.clamp(r, 0, 1)),
				(int) (0.5 + 0xff * Mth.clamp(g, 0, 1)),
				(int) (0.5 + 0xff * Mth.clamp(b, 0, 1)),
				(int) (0.5 + 0xff * Mth.clamp(a, 0, 1))
		);
	}

	public Color(int rgba) {
		value = rgba;
	}

	public Color(int rgb, boolean hasAlpha) {
		if (hasAlpha) {
			value = rgb;
		} else {
			value = rgb | 0xff_000000;
		}
	}

	public Color copy() {
		return copy(true);
	}

	public Color copy(boolean mutable) {
		if (mutable)
			return new Color(value);
		else
			return new Color(value).setImmutable();
	}

	public Color setImmutable() {
		this.mutable = false;
		return this;
	}

	public int getRed() {
		return (getRGB() >> 16) & 0xff;
	}

	public int getGreen() {
		return (getRGB() >> 8) & 0xff;
	}

	public int getBlue() {
		return (getRGB() >> 0) & 0xff;
	}

	public int getAlpha() {
		return (getRGB() >> 24) & 0xff;
	}

	public float getRedAsFloat() {
		return getRed() / 255f;
	}

	public float getGreenAsFloat() {
		return getGreen() / 255f;
	}

	public float getBlueAsFloat() {
		return getBlue() / 255f;
	}

	public float getAlphaAsFloat() {
		return getAlpha() / 255f;
	}

	public int getRGB() {
		return value;
	}

	public Vec3 asVector() {
		return new Vec3(getRedAsFloat(), getGreenAsFloat(), getBlueAsFloat());
	}

	public Vector3f asVectorF() {
		return new Vector3f(getRedAsFloat(), getGreenAsFloat(), getBlueAsFloat());
	}

	public Color setRed(int r) {
		return ensureMutable().setRedUnchecked(r);
	}

	public Color setGreen(int g) {
		return ensureMutable().setGreenUnchecked(g);
	}

	public Color setBlue(int b) {
		return ensureMutable().setBlueUnchecked(b);
	}

	public Color setAlpha(int a) {
		return ensureMutable().setAlphaUnchecked(a);
	}

	public Color setRed(float r) {
		return ensureMutable().setRedUnchecked((int) (0xff * Mth.clamp(r, 0, 1)));
	}

	public Color setGreen(float g) {
		return ensureMutable().setGreenUnchecked((int) (0xff * Mth.clamp(g, 0, 1)));
	}

	public Color setBlue(float b) {
		return ensureMutable().setBlueUnchecked((int) (0xff * Mth.clamp(b, 0, 1)));
	}

	public Color setAlpha(float a) {
		return ensureMutable().setAlphaUnchecked((int) (0xff * Mth.clamp(a, 0, 1)));
	}

	public Color scaleAlpha(float factor) {
		return ensureMutable().setAlphaUnchecked((int) (getAlpha() * Mth.clamp(factor, 0, 1)));
	}

	public Color mixWith(Color other, float weight) {
		return ensureMutable()
				.setRedUnchecked((int) (getRed() + (other.getRed() - getRed()) * weight))
				.setGreenUnchecked((int) (getGreen() + (other.getGreen() - getGreen()) * weight))
				.setBlueUnchecked((int) (getBlue() + (other.getBlue() - getBlue()) * weight))
				.setAlphaUnchecked((int) (getAlpha() + (other.getAlpha() - getAlpha()) * weight));
	}

	public Color darker() {
		int a = getAlpha();
		return ensureMutable().mixWith(BLACK, .25f).setAlphaUnchecked(a);
	}

	public Color brighter() {
		int a = getAlpha();
		return ensureMutable().mixWith(WHITE, .25f).setAlphaUnchecked(a);
	}

	public Color setValue(int value) {
		return ensureMutable().setValueUnchecked(value);
	}

	public Color modifyValue(UnaryOperator<Integer> function) {
		int newValue = function.apply(value);
		if (newValue == value)
			return this;
		return ensureMutable().setValueUnchecked(newValue);
	}

	protected Color ensureMutable() {
		if (this.mutable)
			return this;
		return new Color(this.value);
	}

	protected Color setRedUnchecked(int r) {
		this.value = (this.value & 0xff_00ffff) | ((r & 0xff) << 16);
		return this;
	}

	protected Color setGreenUnchecked(int g) {
		this.value = (this.value & 0xff_ff00ff) | ((g & 0xff) << 8);
		return this;
	}

	protected Color setBlueUnchecked(int b) {
		this.value = (this.value & 0xff_ffff00) | ((b & 0xff) << 0);
		return this;
	}

	protected Color setAlphaUnchecked(int a) {
		this.value = (this.value & 0x00_ffffff) | ((a & 0xff) << 24);
		return this;
	}

	protected Color setValueUnchecked(int value) {
		this.value = value;
		return this;
	}

	public static Color mixColors(Color c1, Color c2, float w) {
		return new Color(
				(int) (c1.getRed() + (c2.getRed() - c1.getRed()) * w),
				(int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * w),
				(int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * w),
				(int) (c1.getAlpha() + (c2.getAlpha() - c1.getAlpha()) * w)
		);
	}

	public static Color mixColors(Couple<Color> colors, float w) {
		return mixColors(colors.getFirst(), colors.getSecond(), w);
	}
}
