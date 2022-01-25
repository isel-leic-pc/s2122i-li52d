using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;

namespace OldExams.i1718
{
    class Exercicio4
    {
        R Reduce<R>(R val, R acum) {
            return default(R);
        }

        R Map<T, R>(T val) {
            return default(R);
        }

        public R MapReduce<R,T>(T[] elems, R initial) {
            for (int i = 0 ; i < elems.Length; ++i)
                initial = Reduce(Map<T,R>(elems[i]), initial);
            return initial;
        }

        Task<R> ReduceAsync<R>(R val, R acum) {
            return Task.FromResult(default(R));
        }

        Task<R> MapAsync<T, R>(T val) {
            return Task.FromResult(default(R));
        }


        public async Task<R>  MapReduceAsync<R, T>(T[] elems, R initial) {
            Task<R>[] mappedTaskElems = new Task<R>[elems.Length];

            for (int i = 0; i < elems.Length; ++i) {
                mappedTaskElems[i] = MapAsync<T,R>(elems[i]);
            }

            R[] mappedElems = await Task.WhenAll(mappedTaskElems);

            for (int i = 0; i < elems.Length; ++i)
                initial = await ReduceAsync(mappedElems[i] , initial);
            return initial;
        }


        // A bad solution since doesn't use potencial paralelism
        public async Task<R> MapReduce2Async<R, T>(T[] elems, R initial) {

            for (int i = 0; i < elems.Length; ++i)
                initial = await ReduceAsync(await MapAsync<T,R>(elems[i]), initial);
            return initial;
        }
    }
}
