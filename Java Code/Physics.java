    public class Physics extends Thread{
    	
        //Volatile Variables
        public volatile static double[] slipAngle = {0, 0};
        public volatile static double[] acceleration = {0, 0};
        public volatile static double[] position = {0, 0};
        public volatile static double[] velocity = {0, 0};
        public volatile static double mCurrAngle = 0;
        public volatile static double mPrevAngle = 0;
        public volatile static double brakingDistance = 0;
        public volatile static double rpm = 1500;
        public volatile static double speed = 0;
        public volatile static double t_dt = 0;
        public volatile static double xPos = 0;
        public volatile static double yPos = 0;
        public volatile static double check = 0;

        //Final Ints
        public final static int x = 0; // x coord constant for vectors
        public final static int y = 1; // y coord constant for vectors
        public final int front = 0;
        public final int rear = 1;

        //Ints
        public static int currentGear = 1;
        public int height = 0, width = 0;
        public int buttonWidth = 0;
        public int buttonHeight = 0;
        public int buttonBaseHeight  = 0;
        public int offset = 0;
        public int gasID = 0;

        //Booleans
        public volatile static Boolean gasOn = false, brakeOn = false, engineOn = false;
		public volatile static Boolean sixtyFound = false;
		public volatile static Boolean impact = false;

        //Floats
        public float xCar = 0;
        public float yCar = 0;
        public double time_start = 0;
        public static double time_end = 0;
    	
        public double[] brakingForce = {0, 0};
        public double[] direction = {0, 1};
        public double[] traction = {0, 0};
        public double[] aerodynamicDrag = {0, 0};
        public double[] rollResistance = {0, 0};
        public double[] totalForce = {0, 0};
        public double[] wheelForce = {0, 0};
        public double[] tempVelocity = {0, 0};
        public volatile static String[] returnVariables = {null, null, null, null, null, null, null, null};

        public double engineForce = 0;
        public double wheelSpeed = 0;
        public double tempRPM = 1500;
        public double steeringAngle = 0;
        public double velocityLongMag = 0;
        
        public final double dragConst = 0.4257; // wind resistance drag constant
        public final double rrConst = 12.8; //roll resistance constant
        public final double mass = 1000; // mass in Kg
        public final double brakeConst = 1000 ; //Play with this value
        public final double diffRatio = 6.82;
        public final double torque = 500; // Newton meters
        public final double transEffecieny = 0.7; // 70% energy transfer from engine to wheels
        public final double wheelRadius = 0.381; // in meters
        public final double angularVelocityConst = 9.55; // 60/(2pi)
        public final double minRPM = 1500;
        public final double[] gearRatio = {2.9, 4.66, 1.78, 1.3, 1.0, 0.74, 0.50}; // 0 = reverse, 1-6 = forward
        
        static double carX;
        static double carY;
    	
        private boolean running;

        public Physics(Server server) {
		}

		public void setRunning(boolean running){
            this.running = running;
        }

        public String[] getVariables(){
        	
        	returnVariables[0] = String.format("%.2f",magnitude(acceleration[x], acceleration[y])); 
        	returnVariables[1] = String.format("%.2f",speed);
        	returnVariables[2] = String.format("%.2f",slipAngle[x]);
        	if(!sixtyFound) returnVariables[3] = "N/A";
        	else returnVariables[3] = String.format("%.2f", time_end);
        	returnVariables[4] = String.format("%.0f",rpm);
        	returnVariables[5] = Double.toString(currentGear);
        	returnVariables[6] = String.format("%.2f",brakingDistance);
        	
        	return returnVariables;
        }
        
        public void run(){
            while(running){
                if(engineOn){
                    if(speed == 0){
                        time_start = (System.currentTimeMillis());
                        sixtyFound = false;
                        impact = false;
                    }
                    if((speed >= 60) && (!sixtyFound)){
                        sixtyFound = true;
                    }
                    if(gasOn)  {
                        engineForce = 20000;
                    }
                    else {
                        engineForce = 0;
                    }
                    if(brakeOn || impact){
                        calculateBrakingForce();
                    }
                    else {
                        brakingForce[x] = 0;
                        brakingForce[y] = 0;
                    }
                    t_dt = .001;
                    setSteeringAngle(mCurrAngle);
                    calculateDirection();
                    calculateVelocity();
                    calculateRollResitance();
                    calculateAerodynamicDrag();
                    calculatePosition();
                    calculateForce();
                    calculateAcceleration();
                    calculateRPM();
                    calculateWheelForce();
                    calculateSpeed();
                    calculateWheelSpeed();
                    calculateCorneringVelocityMag();
                    calculateSlipAngle();
                    calculateBrakingDistance();
                    getVariables();
                }
               try {
				currentThread();
				Thread.sleep(0,5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

            }
        }

        private void setSteeringAngle(double newSteeringAngle){ steeringAngle = newSteeringAngle;}

        private void calculateDirection(){
            if((velocity[x] != 0) || (velocity[y] != 0)) {
                direction[x] = velocity[x] / magnitude(velocity[x], velocity[y]);
                direction[y] = velocity[y] / magnitude(velocity[x], velocity[y]);
            }
        }

        private void calculateSlipAngle(){
            slipAngle[front] = Math.atan((velocity[y] + angularVelocityConst)/velocityLongMag) - steeringAngle;
            slipAngle[rear] = Math.atan((velocity[y] - angularVelocityConst)/velocityLongMag);
        }

        private void calculateCorneringVelocityMag(){
            velocityLongMag = Math.cos(steeringAngle)*velocity[x];
            //  velocityLatMag = Math.sin(steeringAngle)*velocity[y];
        }

        private void calculateWheelSpeed(){
            wheelSpeed = (speed/3.6) / wheelRadius;
        }

        private void calculateRPM(){
            if(gasOn) {
                rpm = wheelSpeed * gearRatio[currentGear] * diffRatio * angularVelocityConst+minRPM;
            }
            else{
                if(rpm > minRPM+10) rpm-=100;
            }
            if((rpm >= 8000) && (currentGear < 6)) currentGear++;
            if((rpm <= 2000) && (currentGear > 1)) currentGear--;
            if((rpm > 8500) && (currentGear == 6)) rpm = 8500;
        }

        private void calculateWheelForce(){
            wheelForce[x] = direction[x]*torque*gearRatio[currentGear]*diffRatio*(transEffecieny/wheelRadius);
            wheelForce[y] = direction[y]*torque*gearRatio[currentGear]*diffRatio*(transEffecieny/wheelRadius);
        }

        private void calculateTraction(){
            traction[x] = direction[x] * engineForce;
            traction[y] = direction[y] * engineForce;
        }

        private void calculateSpeed(){

            if((speed > 0.1) || gasOn) {
                if((speed <= 10) && brakeOn) speed-=1.0;
                else speed = Math.sqrt(velocity[x]*velocity[x] + velocity[y]*velocity[y]);
            }
            else speed = 0;
            if(speed < 0) speed = 0.0;

        }

        private void calculateAerodynamicDrag(){
            aerodynamicDrag[x] = (-dragConst)*(Math.abs(velocity[x]))*speed;
            aerodynamicDrag[y] = (-dragConst)*(Math.abs(velocity[y]))*speed;
        }

        private void calculateForce(){
            totalForce[x] = direction[x]*engineForce + direction[x]*brakingForce[x] + aerodynamicDrag[x] + rollResistance[x];
            totalForce[y] = direction[y]*engineForce + direction[y]*brakingForce[y] + aerodynamicDrag[y] + rollResistance[y];
//            System.out.println(totalForce[x] + ", " + totalForce[y]);
//            System.out.println(direction[x] + ", " + direction[y]);
//            System.out.println(aerodynamicDrag[x] + ", " + aerodynamicDrag[y]);
//            System.out.println(rollResistance[x] + ", " + rollResistance[y]);
        }

        private void calculateRollResitance(){
            rollResistance[x] = (-rrConst)*velocity[x];
            rollResistance[y] = (-rrConst)*velocity[y];
        }

        private void calculateAcceleration(){
            if(gasOn) {
                acceleration[x] = (totalForce[x] / mass);
                acceleration[y] = (totalForce[y] / mass);
            }
            if(!gasOn){
                acceleration[x] = 0;
                acceleration[y] = 0;
            }
        }

        private void calculateVelocity(){
            if((speed > 0) || gasOn) {
                velocity[x] = velocity[x] + t_dt * acceleration[x] + brakingForce[x] * t_dt;
                velocity[y] = velocity[y] + t_dt * acceleration[y] + brakingForce[y] * t_dt;
            }
            else{
                velocity[x] = 0;
                velocity[y] = 0;
            }
        }

        private void calculatePosition(){
            position[x] = position[x] + t_dt * velocity[x];
            position[y] = position[y] + t_dt * velocity[y];
        }

        private void calculateBrakingForce(){
            if(speed > 10) {
                brakingForce[x] = (-1) * direction[x] * (0.1 * brakeConst);
                brakingForce[y] = (-1) * direction[y] * (0.1 * brakeConst);
            }
        }

        private void calculateBrakingDistance(){
            brakingDistance = speed/brakeConst;
        }
        
        static double magnitude(double a, double b){
            return Math.sqrt(a*a + b*b);
        }
    }
