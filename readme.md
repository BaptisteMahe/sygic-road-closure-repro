# This is a bug reproduction repo for Sygic Embedded
_Built by Unico France_

The bug is the following:

After loading [MapCorrections](https://www.sygic.com/developers/professional-navigation-sdk/android/api-examples/online-api),
using`ApiOnline.addMapCorrectionEvents` and trying to start a navigation using `ApiNavigation.startNavigation` sometimes the resulting route 
won't take the road closures into account.

To reproduce the bug using this project:

- Start the project on _android studio_ launching the application on an _emulator_.
- Set the emulator GPS position at _8 Route De La Pinière, 33910 Saint-Denis-de-Pile_ (lat: 44.99843, lon: -0.15698).
- When Sygic is ready to navigate click on _Start navigation_.
- This will start a Navigation towards _Chemin Des Treilles, 33910, ABZAC_.
- 


You can find the applied map corrections at `com/unicofrance/sygic_road_closure_repro/MAP_CORRECTIONS.kt`.

