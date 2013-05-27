package at.bluephoenix.BlobDetector;

import ioio.lib.api.PwmOutput;
import ioio.lib.api.TwiMaster;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;

import java.text.DecimalFormat;

class IOIOcontrol extends BaseIOIOLooper {
    private TwiMaster twi;
    private NervHub data;
    private PwmOutput servo_;

    // looper sensor info
    private String analog[] = new String[9];
    @SuppressWarnings("unused")
    private short xPos;
    @SuppressWarnings("unused")
    private short yPos;
    @SuppressWarnings("unused")
    private short anglePos;

    @Override
    protected void setup() throws ConnectionLostException, InterruptedException {
        super.setup();
        twi = ioio_.openTwiMaster(1, TwiMaster.Rate.RATE_100KHz, false);
        servo_ = ioio_.openPwmOutput(10, 50);
        data = NervHub.getInstance();
    }

    protected void robotForward(int fwd) {
        byte[] request = new byte[2];
        byte[] response = new byte[1];

        request[0] = 0x1C;
        request[1] = (byte) fwd;
        synchronized (twi) {
            try {
                twi.writeRead(0x69, false, request, request.length, response, 0);
            } catch (ConnectionLostException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    protected void robotForward(double fwd) {
        robotForward((int) fwd);
    }

    protected void robotRotate(int grad) {
        byte[] request = new byte[2];
        byte[] response = new byte[1];
        request[0] = 0x1D; // cmd
        request[1] = (byte) grad;
        synchronized (twi) {
            try {
                twi.writeRead(0x69, false, request, request.length, response, 0);
            } catch (ConnectionLostException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected void robotRotate(double grad) {
        robotRotate((int) grad);
    }

    protected void robotMove(int leftMotorSpeed, int rightMotorSpeed) {
        byte[] request = new byte[3];
        byte[] response = new byte[1];
        request[0] = 0x1A;
        request[1] = (byte) rightMotorSpeed;
        request[2] = (byte) leftMotorSpeed;
        synchronized (twi) {
            try {
                twi.writeRead(0x69, false, request, request.length, response, 0);
            } catch (ConnectionLostException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    protected void robotMove(int speed) {
        robotMove(speed, speed);
    }

    protected void robotLED(int blueLED, int redLED) {
        byte[] request = new byte[3];
        byte[] response = new byte[1];
        request[0] = 0x20;
        request[1] = (byte) redLED;// set between 0 and 100
        request[2] = (byte) blueLED;// set between 0 and 100
        synchronized (twi) {
            try {
                twi.writeRead(0x69, false, request, request.length, response, 0);
            } catch (ConnectionLostException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    protected void robotLED(int intensity) {
        robotLED(intensity, intensity);
    }

    protected void gripper(boolean gripperState) {
        try {
            if (gripperState) {
                servo_.setDutyCycle(0.0001f);

            } else {
                servo_.setDutyCycle(0.0528f);

            }
        } catch (ConnectionLostException e) {
            e.printStackTrace();
        }

    }

    protected void robotReadSensor() {
        byte[] request = new byte[] { 0x10 };
        byte[] response = new byte[8];
        synchronized (twi) {
            try {
                twi.writeRead(0x69, false, request, request.length, response,
                        response.length);
            } catch (ConnectionLostException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        for (int l = 0; l < 7; l++) {
            int i = 0xFF & response[l + 1];
            if (l != 0)
                analog[l] = i + "cm";
            else
                analog[l] = new DecimalFormat("#.#").format(i / 10.0) + "V";
        }

        request[0] = 0x1A; // get velocity
        synchronized (twi) {
            try {
                twi.writeRead(0x69, false, request, request.length, response, 2);
            } catch (ConnectionLostException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        analog[7] = response[0] + "";
        analog[8] = response[1] + "";

        /* get position */
        request[0] = 0x1B; // get position
        synchronized (twi) {
            try {
                twi.writeRead(0x69, false, request, request.length, response, 6);
            } catch (ConnectionLostException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        xPos = (short) (((response[1] & 0xFF) << 8) | (response[0] & 0xFF));
        yPos = (short) (((response[3] & 0xFF) << 8) | (response[2] & 0xFF));
        anglePos = (short) (((response[5] & 0xFF) << 8) | (response[4] & 0xFF));

    }

    @Override
    public void loop() throws ConnectionLostException, InterruptedException {
        super.loop();

        switch (data.getMotion().getHookState()) {
        case Down:
            robotLED(100, 0);
            // gripper(true);
            break;
        case Up:
            robotLED(0, 100);
            // gripper(false);
            break;
        default:
            break;
        }

        switch (data.getMotion().getMotorState()) {
        case Backward:
            robotMove(-15);
            break;
        case Forward:
            robotMove(15);
            break;
        case Left:
            robotMove(0, 15);
            break;
        case Right:
            robotMove(15, 0);
            break;
        case Stop:
            robotMove(0);
            break;
        default:
            break;
        }

    }

    @Override
    public void disconnected() {
        super.disconnected();
    }

    @Override
    public void incompatible() {
        super.incompatible();
    }

}