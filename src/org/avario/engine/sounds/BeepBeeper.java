package org.avario.engine.sounds;

import org.avario.engine.DataAccessObject;
import org.avario.engine.prefs.Preferences;
import org.avario.engine.sounds.tones.LiftTone;
import org.avario.engine.sounds.tones.PrenotifyTone;
import org.avario.engine.sounds.tones.SinkTone;
import org.avario.ui.VarioMeterScaleUpdater;
import org.avario.utils.Logger;
import org.avario.utils.Speaker;
import org.avario.utils.StringFormatter;
import org.avario.utils.UnitsConverter;

import android.app.Activity;
import android.os.AsyncTask;

public class BeepBeeper extends AsyncTask<Integer, Float, Integer> {

	private static BeepBeeper THIS;

	private final AsyncTone liftBeep = new LiftTone();
	private final AsyncTone sinkBeep = new SinkTone();
	private final AsyncTone prenotifyBeep = new PrenotifyTone();

	protected BeepBeeper() {
	}

	public static void init(Activity context) {
		THIS = new BeepBeeper();
		THIS.execute();
	}

	public static void clear() {
		THIS.stop();
		THIS.cancel(true);
	}

	@Override
	protected Integer doInBackground(Integer... beeps) {
		while (!THIS.isCancelled()) {
			try {
				float beepSpeed = DataAccessObject.get().getLastVSpeed();
				publishProgress(beepSpeed);
				beepSpeed = beepSpeed > 5 ? 5 : beepSpeed;
				beepSpeed = beepSpeed < -5 ? -5 : beepSpeed;
				if (!validateThisSpeed(beepSpeed)) {
					Thread.sleep(150);
				} else {
					if (beepSpeed > 0) {
						final float beepHz = Preferences.lift_hz + Preferences.tone_variation * beepSpeed;
						liftBeep.setHz(beepHz);
						liftBeep.setSpeed(beepSpeed);
						liftBeep.beep();
					} else if (beepSpeed < 0) {
						final float beepHz = Preferences.sink_hz + (Preferences.tone_variation * 0.3f) * beepSpeed;
						sinkBeep.setHz(beepHz);
						sinkBeep.setSpeed(beepSpeed);
						sinkBeep.beep();
					}
					if (Preferences.use_speach) {
						float saySpeed = UnitsConverter.toPreferredVSpeed(beepSpeed);
						Speaker.get()
								.say(Preferences.units_system == 2 ? StringFormatter.noDecimals(saySpeed) : StringFormatter
										.oneDecimal(saySpeed));
					}
					Thread.sleep(Math.round(250 - 30 * beepSpeed));
				}
			} catch (InterruptedException e) {
				break;
			} catch (Exception ex) {
				Logger.get().log("Fail in beep: ", ex);
			}
		}

		return null;
	}

	@Override
	protected void onProgressUpdate(final Float... speed) {
		try {
			float vSpeed = Math.abs(speed[0]) > Preferences.lift_start ? speed[0] : 0.0f;
			VarioMeterScaleUpdater.getInstance().updateSpeed(vSpeed);
		} catch (Exception ex) {
			Logger.get().log("Fail async beep progress: ", ex);
		}
	}

	private boolean validateThisSpeed(float beepSpeed) {
		if (beepSpeed == Float.NaN) {
			return false;
		}

		if (Math.abs(beepSpeed) < 0.1) {
			prenotifyThermal();
		}

		if (Math.abs(beepSpeed) < Preferences.lift_start) {
			return false;
		}

		if (beepSpeed <= 0f && beepSpeed > Preferences.sink_start) {
			return false;
		}

		if (Math.abs(beepSpeed) > 15) {
			return false;
		}

		return true;
	}

	private void prenotifyThermal() {
		if (Preferences.prenotify_interval > 0 && DataAccessObject.get().isGPSFix()
				&& UnitsConverter.msTokmh(DataAccessObject.get().getLastlocation().getSpeed()) > 5) {
			prenotifyBeep.beep();
		}
	}

	public void stop() {
		this.cancel(true);
	}

}
