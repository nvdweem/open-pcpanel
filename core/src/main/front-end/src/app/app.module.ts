import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';

import {AppRoutingModule} from './app-routing.module';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MainComponent} from './main/main.component';
import {LayoutModule} from '@angular/cdk/layout';
import {MatToolbarModule} from '@angular/material/toolbar';
import {MatButtonModule} from '@angular/material/button';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatIconModule} from '@angular/material/icon';
import {MatListModule} from '@angular/material/list';
import {ControlConfigurationComponent} from './control-configuration/control-configuration.component';
import {DeviceComponent} from './device/device.component';
import {SelectDeviceComponent} from './select-device/select-device.component';
import {MatTabsModule} from '@angular/material/tabs';
import {ClickableComponent} from './config/clickable/clickable.component';
import {AnalogComponent} from './config/analog/analog.component';
import {LightComponent} from './config/light/light.component';
import {HttpClientModule} from '@angular/common/http';
import {ColorPickerModule} from '@iplab/ngx-color-picker';
import {ReactiveFormsModule} from '@angular/forms';
import {MatSliderModule} from '@angular/material/slider';
import {MatCheckboxModule} from '@angular/material/checkbox';

@NgModule({
  declarations: [
    MainComponent,
    ControlConfigurationComponent,
    DeviceComponent,
    SelectDeviceComponent,
    ClickableComponent,
    AnalogComponent,
    LightComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    HttpClientModule,
    LayoutModule,
    MatToolbarModule,
    MatButtonModule,
    MatSidenavModule,
    MatIconModule,
    MatListModule,
    MatTabsModule,
    ColorPickerModule,
    ReactiveFormsModule,
    MatSliderModule,
    MatCheckboxModule,
  ],
  providers: [],
  bootstrap: [MainComponent]
})
export class AppModule {
}
