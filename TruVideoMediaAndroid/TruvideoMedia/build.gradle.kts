plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.truvideo.media"
    compileSdk = 35

    defaultConfig {
        minSdk = 24
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

}

// Create configuration for copyDependencies
configurations {
    create("copyDependencies")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.truvideo.sdk.android.media)
    configurations["copyDependencies"].dependencies.add(
        project.dependencies.create("com.github.Truvideo:truvideo-sdk-android-media:76.2.5")
    )

}


// Copy dependencies for binding library
project.afterEvaluate {
    tasks.register<Copy>("copyDeps") {
        // Copy both .aar and .jar files
        from(configurations["copyDependencies"].filter {
            it.name.endsWith(".aar") || it.name.endsWith(".jar")
        })
        into("${buildDir}/outputs/deps")
    }
    tasks.named("preBuild") { finalizedBy("copyDeps") }
}