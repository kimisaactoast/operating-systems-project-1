package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    static BoatGrader bg;

    static Lock lock;
    static Condition2 oahuChildCond;
    static Condition2 oahuAdultCond;
    static Condition2 molokaiChildCond;
    static Condition2 passengerCond;
	static Condition2 rideCond;

    static int childrenOnOahu;
    static int adultsOnOahu;
    static int childrenOnMolokai;

    static boolean boatOnOahu;
    static boolean pilotWaiting;

    static int totalPeople;
    static Lock doneLock;
    static Condition2 doneCond;
    static boolean done;

    public static void selfTest()
    {
	BoatGrader b = new BoatGrader();
	
	System.out.println("\n ***Testing Boats with only 2 children***");
	begin(0, 2, b);

	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
 	begin(1, 2, b);

 	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
 	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
	// Store the externally generated autograder in a class
	// variable to be accessible by children.
	bg = b;

	// Instantiate global variables here
	lock = new Lock();
	oahuChildCond = new Condition2(lock);
	oahuAdultCond = new Condition2(lock);
	molokaiChildCond = new Condition2(lock);
	passengerCond = new Condition2(lock);
	rideCond = new Condition2(lock);

	childrenOnOahu = children;
	adultsOnOahu = adults;
	childrenOnMolokai = 0;
	boatOnOahu = true;
	pilotWaiting = false;

	totalPeople = adults + children;
	done = false;
	doneLock = new Lock();
	doneCond = new Condition2(doneLock);

	for (int i = 0; i < adults; i++) {
		new KThread(new Runnable() {
			public void run() { AdultItinerary(); }
		}).setName("Adult").fork();
	}
	for (int i = 0; i < children; i++) {
		new KThread(new Runnable() {
			public void run() { ChildItinerary(); }
		}).setName("Child").fork();
	}

	doneLock.acquire();
	while (!done) doneCond.sleep();
	doneLock.release();
    }

    static void AdultItinerary()
    {
	/* This is where you should put your solutions. Make calls
	   to the BoatGrader to show that it is synchronized. For
	   example:
	       bg.AdultRowToMolokai();
	   indicates that an adult has rowed the boat across to Molokai
	*/

	lock.acquire();

	while (true) {
		if (boatOnOahu && childrenOnOahu <= 1 && !pilotWaiting) {
			adultsOnOahu--;
			boatOnOahu = false;
			lock.release();

			bg.AdultRowToMolokai();

			lock.acquire();
			molokaiChildCond.wake();
			lock.release();

			signalDoneIfFinished();
			return;
		}
		oahuAdultCond.sleep();
	}
    }

    static void ChildItinerary()
    {
	boolean onOahu = true;

	lock.acquire();

	while (true) {
		if (onOahu) {
			while (!boatOnOahu) {
				oahuChildCond.sleep();
			}

			if (pilotWaiting) {
				pilotWaiting = false;
				boatOnOahu = false;
				childrenOnOahu--;
				childrenOnMolokai++;
				onOahu = false;

				passengerCond.wake();
				rideCond.sleep();

				lock.release();
				bg.ChildRideToMolokai();
				lock.acquire();

				passengerCond.wake();
			} else if (childrenOnOahu >= 2) {
				pilotWaiting = true;
				childrenOnOahu--;

				oahuChildCond.wake();
				passengerCond.sleep();

				childrenOnMolokai++;
				onOahu = false;

				lock.release();
				bg.ChildRowToMolokai();
				lock.acquire();

				rideCond.wake();
				passengerCond.sleep();

				if (adultsOnOahu + childrenOnOahu > 0) {
					childrenOnMolokai--;
					onOahu = true;

					lock.release();
					bg.ChildRowToOahu();
					lock.acquire();
					
					childrenOnOahu++;
					boatOnOahu = true;

					oahuChildCond.wakeAll();
					oahuAdultCond.wakeAll();
				} else {
					molokaiChildCond.wakeAll();
					lock.release();
					signalDoneIfFinished();
					return;
				}
			} else {
				if (adultsOnOahu > 0) {
					oahuAdultCond.wake();
				}
				oahuChildCond.sleep();
			}
		} else {
			molokaiChildCond.sleep();

			if (childrenOnOahu == 0 && adultsOnOahu == 0) {
				lock.release();
				signalDoneIfFinished();
				return;
			}

			childrenOnMolokai--;
			onOahu = true;
			lock.release();

			bg.ChildRowToOahu();

			lock.acquire();
			childrenOnOahu++;
			boatOnOahu = true;

			oahuChildCond.wakeAll();
			oahuAdultCond.wakeAll();
		}
	}
    }

    private static void signalDoneIfFinished()
    {
	lock.acquire();
	boolean finished = (childrenOnOahu == 0 && adultsOnOahu == 0);
	lock.release();

	if (finished) {
		doneLock.acquire();
		done = true;
		doneCond.wakeAll();
		doneLock.release();
	}
    }
}