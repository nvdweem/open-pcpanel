import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable, Subject} from 'rxjs';
import {filter, map, shareReplay, startWith, switchMap} from 'rxjs/operators';

export interface Device {
  states: number[];
  type: 'PCPANEL_PRO';
  id: string;
  activeProfile: Profile;
  profiles: Profile[];
}

export interface Profile {
  id: number;
  device: string;
  name: string;
  lightConfig: any;
  actionsConfig: any;
}

@Injectable({
  providedIn: 'root'
})
export class DeviceService {
  private reload$ = new Subject<void>();
  devices$: Observable<Device[]>;

  constructor(private http: HttpClient) {
    this.devices$ = this.reload$.pipe(startWith(0), switchMap(() => http.get<Device[]>('api/devices').pipe(shareReplay(1))));
  }

  device$(id: string): Observable<Device> {
    return this.devices$.pipe(map(ds => ds.find(d => d.id === id)!), filter(ds => !!ds));
  }

  reload(): void {
    this.reload$.next();
  }
}
