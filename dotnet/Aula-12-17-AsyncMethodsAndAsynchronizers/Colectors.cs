using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;

namespace AsyncIntro
{
    public class Collectors
    {
        // Start of synchronous stuff

        public static R Map<T, R>(T t) { return default(R); }
        public static R Join<R>(R r, R r2) { return default(R); }


        public static R[] MapJoin<T, R>(T[] items) {
            var res = new R[items.Length / 2];
            for (int i = 0; i < res.Length; i++) {
                res[i] = Join(Map<T, R>(items[2 * i]), Map<T, R>(items[2 * i + 1]));

            }
            return res;
        }

        // Start of assynchronous stuff

        public static Task<R> MapAsync<T, R>(T t) {
            return Task.FromResult(default(R));
        }

        public static Task<R> JoinAsync<R>(R r, R r2) {
            return Task.FromResult(default(R));
        }

        /// <summary>
        /// An auxiliary assinchronpus method in order to simplify
        /// the parallel execution of different joins
        /// </summary>
        /// <typeparam name="T"></typeparam>
        /// <typeparam name="R"></typeparam>
        /// <param name="t1"></param>
        /// <param name="t2"></param>
        /// <returns></returns>
        private static async Task<R> MapJoinPairAsync<T, R>(T t1, T t2) {
            Task<R> task1 = MapAsync<T, R>(t1);
            Task<R> task2 = MapAsync<T, R>(t2);
            await Task.WhenAll(task1, task2);
            return await JoinAsync<R>(task1.Result, task2.Result);
        }

        public static async Task<R[]> MapJoinAsync<T, R>(T[] items) {
            var res = new R[items.Length / 2];
            Task<R>[] tasks = new Task<R>[res.Length];

            for (int i = 0; i < res.Length; i++) {
                tasks[i] = MapJoinPairAsync<T, R>(items[i], items[i + 1]);
            }
            await Task.WhenAll(tasks);
            for (int i = 0; i < res.Length; i++) {
                res[i] = tasks[i].Result;
            }
            return res;
        }
    }    
}
