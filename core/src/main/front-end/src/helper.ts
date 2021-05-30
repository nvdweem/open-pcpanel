import {ActivatedRoute, ParamMap, Params} from '@angular/router';
import {combineLatest, Observable} from 'rxjs';
import {map} from 'rxjs/operators';

export function combineAllParams(route: ActivatedRoute | null): Observable<Params> {
  const paramMaps: Observable<ParamMap>[] = [];
  while (route) {
    paramMaps.push(route.paramMap);
    route = route.parent;
  }
  return combineLatest(paramMaps).pipe(map(ps => {
    const result: Params = {};
    for (const p of ps) {
      for (const key of p.keys) {
        result[key] = String(p.get(key));
      }
    }
    return result;
  }));
}
