package espada.spacex.aurora.modules.timer.modes;

import espada.spacex.aurora.modules.timer.TimerMode;
import espada.spacex.aurora.modules.timer.TimerPlus;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;

import static espada.spacex.aurora.modules.timer.TimerPlus.*;

public class NCP extends TimerMode {
	private Timer timer;
	public NCP() {
		super(TimerPlus.TimerModes.NCP);
		timer = Modules.get().get(Timer.class);
	}

	@Override
	public void onDeactivate() {
		timer.setOverride(Timer.OFF);
	}

	@Override
	public void onTickEventPre(TickEvent.Pre event) {
		if (mc.player == null) return;
		if (rechargeTimer == 0) {
			if (workingTimer > workingDelay) {
				rechargeTimer = rechargeDelay;
				workingTimer = 0;
				timer.setOverride(Timer.OFF);
			}
			else {
				if (settings.isActive()) {
					if (settings.onlyInMove.get() && PlayerUtils.isMoving()) {
						workingTimer++;
						timer.setOverride(timerMultiplier);
					}
					else if (!settings.onlyInMove.get()) {
						workingTimer++;
						timer.setOverride(timerMultiplier);
					}
					else {
						timer.setOverride(timerMultiplierOnRecharge);
					}
				}
			}
		}
		else {
			rechargeTimer--;
			if (settings.isActive()) {
				timer.setOverride(timerMultiplierOnRecharge);
			}
			else {
				timer.setOverride(Timer.OFF);
			}
		}
	}
}
