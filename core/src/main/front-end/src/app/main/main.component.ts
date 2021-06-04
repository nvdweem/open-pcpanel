import {ChangeDetectionStrategy, Component} from '@angular/core';
import {BreakpointObserver, Breakpoints} from '@angular/cdk/layout';
import {Observable} from 'rxjs';
import {map, shareReplay} from 'rxjs/operators';

@Component({
  selector: 'pcp-main',
  templateUrl: './main.component.html',
  styleUrls: ['./main.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MainComponent {
  private _darkMode = true;

  isHandset$: Observable<boolean> = this.breakpointObserver.observe(Breakpoints.Handset)
    .pipe(
      map(result => result.matches),
      shareReplay()
    );

  constructor(private breakpointObserver: BreakpointObserver) {
    this.darkMode = localStorage.getItem('darkmode') !== 'false';
  }

  toggleDarkMode(): void {
    this.darkMode = !this.darkMode;
  }

  set darkMode(dm: boolean) {
    if (!dm) {
      document.body.classList.add('light');
    } else {
      document.body.classList.remove('light');
    }
    localStorage.setItem('darkmode', String(dm));
    this._darkMode = dm;
  }

  get darkMode(): boolean {
    return this._darkMode;
  }
}
