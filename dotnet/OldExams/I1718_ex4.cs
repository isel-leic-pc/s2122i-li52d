using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;

namespace OldExams
{
    class I1718_ex4
    {
        public static   R Reduce<R>(R elem, R acum) {
            return default(R);
        }

        public static   R Map<T,R>(T val) {
            return default(R);
        }

        public R MapReduce<T,R>(T[] elems, R initial) {
            for (int i = 0 ; i < elems.Length; ++i)
                initial = Reduce(Map<T,R>(elems[i]), initial);
            return initial;
        }


        // Async Version
        public static Task<R> ReduceAsync<R>(R elem, R acum) {
            return Task.FromResult(default(R));
        }

        public static Task<R> MapAsync<T, R>(T val) {
            return Task.FromResult(default(R));
        }

        public async Task<R> MapReduceAsync<T, R>(T[] elems, R initial) {
            Task<R>[] mappedVals = new Task<R>[elems.Length];
            for (int i = 0; i < elems.Length; ++i)
                mappedVals[i] = MapAsync<T, R>(elems[i]);
            for (int i = 0; i < mappedVals.Length; ++i)
               initial = await ReduceAsync(await mappedVals[i], initial);
            return initial;
        }
    }
}
