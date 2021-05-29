import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {ControlConfigurationComponent} from './control-configuration/control-configuration.component';
import {DeviceComponent} from './device/device.component';
import {SelectDeviceComponent} from './select-device/select-device.component';
import {ClickableComponent} from './config/clickable/clickable.component';
import {AnalogComponent} from './config/analog/analog.component';
import {LightComponent} from './config/light/light.component';

const routes: Routes = [
  {
    path: ':device/:profile', component: DeviceComponent, children: [
      {
        path: ':type/:number', component: ControlConfigurationComponent, children: [
          {path: 'click', component: ClickableComponent},
          {path: 'analog', component: AnalogComponent},
          {path: 'light', component: LightComponent},
        ]
      },
    ]
  },
  {path: '', component: SelectDeviceComponent},
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {
}
