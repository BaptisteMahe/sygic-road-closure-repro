# This is a bug reproduction repo for Sygic Embedded

The bug is the following:

After loading [MapCorrections](https://www.sygic.com/developers/professional-navigation-sdk/android/api-examples/online-api),
using `ApiOnline.addMapCorrectionEvents` and trying to start a navigation using `ApiNavigation.startNavigation` sometimes the resulting route 
won't take the road closures into account.

The app has the following buttons:
- **Load map** corrections will load map correction
- **Clear map corrections** will clear the map correction
- **Start navigation** will start a navigation to _Chemin Des Treilles, 33910, ABZAC_ (lat: 45.002110, lon:-0.135820)
- **Break state** will start a navigation and load map corrections _in parallel_. In my testing this does not break app 100% of the time but it will do so pretty often.

To reproduce the bug using this project:

- Start the project on _android studio_ launching the application on an _emulator_.
- Set the emulator GPS position at _8 Route De La Pinière, 33910 Saint-Denis-de-Pile_ (lat: 44.99843, lon: -0.15698).
- When Sygic is ready:
  - If you try the button on the left (load, clear and start) you can see that Sygic is behaving well and respecting the road closures.
  - When you click on _Break state_
    - (In my testing it's almost 100% of the time but not this could result in some rare cases in a non broken state) 
    - Now if you try to Start navigation again or load/clear the map corrections Sygic's routing won't respect them.

Reproduction example:
![](https://objectstorage.eu-paris-1.oraclecloud.com/p/aeM8-rmRRIv6odSob-U9qi-UMAw5trnFaCgsiUN-MBJ8e__t6Ugcx26ytue_v4ek/n/axcbk7kkisob/b/external_content/o/sygic-repro-example.mov)
