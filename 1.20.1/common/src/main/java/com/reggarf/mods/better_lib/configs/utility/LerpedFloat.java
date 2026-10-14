package com.reggarf.mods.better_lib.configs.utility;

import net.minecraft.util.Mth;

public class LerpedFloat {

	protected Interpolator interpolator;
	protected float previousValue;
	protected float value;

	protected Chaser chaseFunction;
	protected float chaseTarget;
	protected float chaseSpeed;

	public LerpedFloat(Interpolator interpolator) {
		this.interpolator = interpolator;
		startWithValue(0);
	}

	public static LerpedFloat linear() {
		return new LerpedFloat((p, c, t) -> (float) Mth.lerp(p, c, t));
	}

	public LerpedFloat startWithValue(double value) {
		float f = (float) value;
		this.previousValue = f;
		this.chaseTarget = f;
		this.value = f;
		return this;
	}

	public LerpedFloat chase(double value, double speed, Chaser chaseFunction) {
		this.chaseTarget = (float) value;
		this.chaseSpeed = (float) speed;
		this.chaseFunction = chaseFunction;
		return this;
	}

	public void tickChaser() {
		previousValue = value;
		if (chaseFunction == null)
			return;
		if (Mth.equal((double) value, chaseTarget)) {
			value = chaseTarget;
			return;
		}
		value = chaseFunction.chase(value, chaseSpeed, chaseTarget);
	}

	public void setValue(double value) {
		this.previousValue = this.value;
		this.value = (float) value;
	}

	public float getValue() {
		return getValue(1);
	}

	public float getValue(float partialTicks) {
		return interpolator.interpolate(partialTicks, previousValue, value);
	}

	public boolean settled() {
		return Mth.equal((double) previousValue, value) && (chaseFunction == null || Mth.equal((double) value, chaseTarget));
	}

	public float getChaseTarget() {
		return chaseTarget;
	}

	public LerpedFloat bump(int steps, float force) {
		this.value += force;
		return this;
	}

	@FunctionalInterface
	public interface Interpolator {
		float interpolate(double progress, double current, double target);
	}

	@FunctionalInterface
	public interface Chaser {
		Chaser IDLE = (c, s, t) -> (float) c;
		Chaser EXP = exp(Double.MAX_VALUE);
		Chaser LINEAR = (c, s, t) -> (float) (c + Mth.clamp(t - c, -s, s));

		static Chaser exp(double maxEffectiveSpeed) {
			return (c, s, t) -> (float) (c + Mth.clamp((t - c) * s, -maxEffectiveSpeed, maxEffectiveSpeed));
		}

		float chase(double current, double speed, double target);
	}
}
