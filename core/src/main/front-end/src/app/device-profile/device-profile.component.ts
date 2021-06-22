import {ChangeDetectionStrategy, Component} from '@angular/core';
import {DeviceService, Profile} from '../service/device.service';
import {ActivatedRoute, Router} from '@angular/router';
import {combineLatest, merge, Observable, Subject} from 'rxjs';
import {filter, map, switchMap, tap} from 'rxjs/operators';
import {HttpClient} from '@angular/common/http';
import {FileService} from '../service/file.service';

@Component({
  selector: 'pcp-device-profile',
  templateUrl: './device-profile.component.html',
  styleUrls: ['./device-profile.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class DeviceProfileComponent {
  private newProfile$ = new Subject<string | null>();
  deviceId = '';
  profiles$: Observable<Profile[]>;

  constructor(private http: HttpClient,
              private deviceService: DeviceService,
              private router: Router,
              private route: ActivatedRoute,
              private download: FileService) {
    const newProfile$ = this.newProfile$.pipe(
      filter(n => !!n),
      switchMap(n => this.doNewProfile(n!))
    );

    const initialProfile$ = combineLatest([deviceService.devices$, route.paramMap]).pipe(map(([devices, params]) => {
      this.deviceId = String(params.get('device'));
      const fDevices = devices.filter(d => d.id === this.deviceId);
      if (fDevices.length === 0) {
        console.error(this.deviceId, 'is not a valid device');
        return [];
      }
      const device = fDevices[0];
      if (!route.firstChild && device.profiles.length !== 0) {
        router.navigate([`${device.id}/${device.profiles[0].id}`], {replaceUrl: true});
      }

      return device.profiles;
    }));

    this.profiles$ = merge(initialProfile$, newProfile$).pipe(tap(ps => ps.sort((l, r) => l.name.localeCompare(r.name))));
  }

  newProfile(): void {
    this.newProfile$.next(prompt('Name for new profile'));
  }

  private doNewProfile(name: string): Observable<Profile[]> {
    return this.http.post<Profile[]>(`api/profile/${this.deviceId}`, {}, {params: {name}});
  }

  forceNavigate(event: MouseEvent, id: number): void {
    this.router.navigate(['.'], {replaceUrl: false, relativeTo: this.route}).then(() => this.router.navigate([id], {relativeTo: this.route}));
    event.preventDefault();
  }

  trackProfile(idx: number, p: Profile) {
    return p.id;
  }

  rename(profile: Profile): void {
    profile.name = prompt('New name', profile.name) || profile.name;
    this.http.put<Profile[]>(`api/profile/${this.deviceId}`, profile).subscribe(() => this.deviceService.reload());
  }

  delete(profile: Profile): void {
    if (confirm(`Are you sure you want to delete profile '${profile.name}'`)) {
      this.http.delete<Profile[]>(`api/profile/${this.deviceId}/${profile.id}`).subscribe(() => this.deviceService.reload());
    }
  }

  export(profile: Profile): void {
    this.download.download(JSON.stringify(profile, null, 2), `${profile.name}.json`, 'application/json');
  }

  import(targetProfile?: Profile): void {
    this.download.upload().subscribe(fs => {
      if (fs.length > 0) {
        const reader = new FileReader();
        reader.readAsText(fs.item(0)!, 'UTF-8');
        reader.onload = () => {
          const profile = JSON.parse(reader.result as string) as Profile;
          if (targetProfile) {
            profile.id = targetProfile.id;
            profile.name = targetProfile.name;
          }
          this.http.put<Profile[]>(`api/profile/${this.deviceId}`, profile).subscribe(() => this.deviceService.reload());
        };
      }
    });
  }
}
