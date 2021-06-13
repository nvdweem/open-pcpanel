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
import {ActionComponent} from './config/action/action.component';
import {LightComponent} from './config/light/light.component';
import {HttpClientModule} from '@angular/common/http';
import {ColorPickerModule} from '@iplab/ngx-color-picker';
import {ReactiveFormsModule} from '@angular/forms';
import {MatSliderModule} from '@angular/material/slider';
import {MatCheckboxModule} from '@angular/material/checkbox';
import {DeviceProfileComponent} from './device-profile/device-profile.component';
import {ConfigPanelComponent} from './config/config-panel/config-panel.component';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS, MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatOptionModule} from '@angular/material/core';
import {MatDialogModule} from '@angular/material/dialog';
import {FilePickerComponent} from './config/file-picker/file-picker.component';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {ProComponent} from './device/pro/pro.component';
import {MatRadioModule} from '@angular/material/radio';

@NgModule({
  declarations: [
    MainComponent,
    ControlConfigurationComponent,
    DeviceComponent,
    SelectDeviceComponent,
    ActionComponent,
    LightComponent,
    DeviceProfileComponent,
    ConfigPanelComponent,
    FilePickerComponent,
    ProComponent,
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
    MatFormFieldModule,
    MatInputModule,
    MatAutocompleteModule,
    MatOptionModule,
    MatDialogModule,
    MatProgressSpinnerModule,
    MatRadioModule,
  ],
  providers: [
    {
      provide: MAT_FORM_FIELD_DEFAULT_OPTIONS,
      useValue: {
        appearance: 'fill'
      },
    }
  ],
  bootstrap: [MainComponent]
})
export class AppModule {
}
