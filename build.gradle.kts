// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.3.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    //enable KSP processor used by Room
    id("com.google.devtools.ksp") version "1.9.10-1.0.13" apply false
    // added for firebase use
    id("com.google.gms.google-services") version "4.4.1" apply false
}