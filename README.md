# Gofo Tezov project (Old Java)

You'll find here the sources of the application Gofo only (need to take the lib from [crypter](https://github.com/tezov/crypter_and_lib)).

https://play.google.com/store/apps/details?id=com.tezov.gofo.rse&hl=en&gl=US

This project has been done with the same lib than Crypter. 

This project is a mix up of Java / kotlin with a crazy attempt to bridge my lib and a half part of coroutine and multi-thread in Java. This is code everything you should not do. But add the end, I obtain a very successful recycler view with load on the fly.

This project use an Unplash free API key with a REST API. I didn't hide it, you find it in the source. therefore, the free api is limmited, so if you are overtesting this app, go retry an API key yourself.

## How to install

- Download this project
- Download all the lib folder (lib_java / lib_java_android / pluginTezov) from [crypter](https://github.com/tezov/crypter_and_lib)
- Put the all the 4 folders in a single folder
- Open the app_gofo folder with android studio
- sync and build.

If you don't understand the folder strutures you need to comply, look the crypter project first [crypter](https://github.com/tezov/crypter_and_lib).


## What's powerfull in there ?

This file is what's make everythiing possible:

https://github.com/tezov/gofo/blob/master/app/src/main/kotlin/com/tezov/lib/adapterJavaToKotlin/async/IteratorBufferAsync_K.kt

Like I said, it is a crazy mix Java / Kotlin with thread safe, but this little guy can find food and dispatch them without any mistake making the recycler view hyper responsive.
