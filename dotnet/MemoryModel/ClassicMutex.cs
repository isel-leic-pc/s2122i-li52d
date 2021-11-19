using System;
using System.Threading;

namespace ClassicMutex
{
    class ClassicLock
    {
        // An array of two boolean,
        // for telling the threads interest in lock acquisition.
        // The access to the array elements is done through
        // static method of Volatile class in order to have
        // volatile access semantics
        private bool[] interested = new bool[2];

        volatile private int turn;

        public void EnterCritical(int i) {
            int other = 1 - i; // the other thread id
            Volatile.Write(ref interested[i], true);
            turn = other;
            // the memory barrier in next line is crucial to 
            // avoid observered reorders between the instructions
            // above and below the barrier
            Thread.MemoryBarrier();
            while (Volatile.Read(ref interested[other]) && turn == other) ;
            
        }

        public void LeaveCritical(int i) {
            Volatile.Write(ref interested[i], false);
        }

        private const int NTRIES = 20000000;
        static volatile int count;
        static volatile ClassicLock mutex = new ClassicLock();

        private static void CounterThread(int id) {
            for (int i = 0; i < NTRIES; ++i) {
                mutex.EnterCritical(id);
                count += 1;
                mutex.LeaveCritical(id);
            }
        }


        public static void Main(String[] args) {

            while (true) {
                long startTicks;

                count = 0;
                startTicks = Environment.TickCount;
                Thread t1 = new Thread(()=>CounterThread(0));
                Thread t2 = new Thread(()=>CounterThread(1));

                t1.Start();
                t2.Start();

                t1.Join();
                t2.Join();


                Console.WriteLine("Expected = {0}, Real={1} in {2}ms!\n",
                     NTRIES * 2, count, Environment.TickCount - startTicks);
        

            }
        }

    }
}
