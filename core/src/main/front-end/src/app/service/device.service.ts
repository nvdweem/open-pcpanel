import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {shareReplay} from 'rxjs/operators';

export interface Device {
  states: number[];
  type: 'PCPANEL_PRO';
  id: string;
}

@Injectable({
  providedIn: 'root'
})
export class DeviceService {
  devices$: Observable<Device[]>;

  constructor(private http: HttpClient) {
    this.devices$ = http.get<Device[]>('/api/devices').pipe(shareReplay(1));
  }
}