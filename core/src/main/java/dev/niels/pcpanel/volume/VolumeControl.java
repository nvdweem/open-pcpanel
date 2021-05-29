package dev.niels.pcpanel.volume;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

@Slf4j
public class VolumeControl {
    public static void xmain(String... args) {
        System.out.println("Test!");

        var dIds = new ArrayList<String>();
        var sIds = new ArrayList<String>();
        WindowsSndLibrary.INSTANCE.init(
                (a, b) -> {
                    log.error("Device added!!! {}, {}", a, b);
                    dIds.add(b.toString());
                },
                a -> log.error("Device removed: {}", a),
                (a, b) -> {
                    log.error("    Session added!!! {}, {}", a, b);
                    sIds.add(b.toString());
                },
                a -> log.error("    Session removed: {}", a)
        );

//        dIds.stream().map(WString::new).forEach(id -> WindowsSndLibrary.INSTANCE.setDeviceVolume(id, 100, true));
//        sIds.stream().map(WString::new).forEach(id -> WindowsSndLibrary.INSTANCE.setProcessVolume(id, 100, true));

        WindowsSndLibrary.INSTANCE.setFgProcessVolume(50, true);

//        CLibrary.INSTANCE.printf("Dit is een TEST!!!");

//        JNAerator.main(new String[]{
////                "-F", "C:\\Program Files (x86)\\Windows Kits\\10\\Include\\10.0.19041.0\\um",
////                "-F", "C:\\Program Files (x86)\\Windows Kits\\10\\Include\\10.0.19041.0\\shared",
//                "-I", "C:\\Program Files (x86)\\Windows Kits\\10\\Include\\10.0.19041.0\\shared",
//                "-I", "C:\\Program Files (x86)\\Windows Kits\\10\\Include\\10.0.19041.0\\um",
//                "-I", "C:\\Program Files (x86)\\Windows Kits\\10\\Include\\10.0.19041.0\\ucrt",
//                "-I", "C:\\Program Files (x86)\\Microsoft Visual Studio\\2019\\Community\\VC\\Tools\\MSVC\\14.29.30037\\include",
//                "-runtime", "JNA",
//                "-noComp", "-noJar",
//                "-mode", "Directory",
//                "C:\\Program Files (x86)\\Windows Kits\\10\\Include\\10.0.19041.0\\um\\mmdeviceapi.h"
//        });


//        for (Mixer.Info info : AudioSystem.getMixerInfo()) {
//            System.out.println(info);
//            var mixer = AudioSystem.getMixer(info);
//            mixer.open();
//            for (var line : mixer.getTargetLineInfo()) {
//                System.out.println("  - " + line);
//                var ml = mixer.getLine(line);
//                ml.open();
//
//                try {
//                    var ctrl = ((FloatControl) ml.getControl(FloatControl.Type.VOLUME));
//                    ctrl.setValue(0);
//                } catch (Exception e) {
//                    System.out.println("    Nope");
//                }
//            }
//        }
    }
}
