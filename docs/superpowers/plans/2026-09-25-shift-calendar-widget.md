# 교대캘린더 위젯 앱 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 안드로이드 홈화면에 한달위젯(4x4)/작은한달위젯(3x2)/일주일위젯(4x1)/하루위젯(1x1) 4종을 제공하고, 본 앱에서 근무 패턴·메모를 관리하며, 위젯마다 배경 투명도/색상/글자 크기/음력 표시를 독립적으로 설정할 수 있는 완전 오프라인 개인용 앱을 만든다.

**Architecture:** Kotlin 단일 모듈(`app`) 프로젝트. 데이터는 Room(SQLite)에 저장하고, 근무 패턴 반복 계산·음력 변환 같은 핵심 로직은 Android 의존성이 없는 순수 Kotlin 함수로 분리해 JUnit으로 테스트한다. 본 앱 화면은 Jetpack Compose, 위젯 4종은 Jetpack Glance로 만들며, 두 UI 계층은 같은 `ShiftRepository`를 공유한다. 위젯 디자인 설정(투명도/색상/글자크기/음력표시/주시작요일)은 Glance의 위젯별 내장 Preferences 상태에 저장해 위젯 인스턴스마다 독립적으로 유지된다.

**Tech Stack:** Kotlin 2.0.21, AGP 8.7.3, Gradle 8.9, JDK 17, compileSdk/targetSdk 35, minSdk 26, Jetpack Compose (BOM 2024.12.01), Jetpack Glance 1.1.1, Room 2.6.1(KSP), KoreanLunarCalendar 0.4.0(JitPack, 오프라인 변환 라이브러리)

**참고 (실행 담당자에게):** 이 문서의 Glance/Compose 관련 정확한 import 경로나 메서드 이름은 실제 컴파일 시 컴파일러 오류 메시지를 보고 미세 조정이 필요할 수 있습니다(예: `androidx.glance.appwidget.state.PreferencesGlanceStateDefinition`의 정확한 패키지 등). 이런 사소한 import/이름 불일치는 빌드 확인 단계에서 바로잡고 넘어가면 되며, 설계 자체를 바꾸는 문제가 아닙니다.

**개발 환경 (이미 준비됨):** 이 컴퓨터에는 아래 도구가 설치되어 있습니다. 모든 `./gradlew` 명령은 아래 환경변수를 먼저 설정한 뒤 실행하세요.

```bash
export JAVA_HOME="/c/dev-tools/jdk-17.0.20.1+1"
export ANDROID_HOME="/c/Android/sdk"
export ANDROID_SDK_ROOT="/c/Android/sdk"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/platform-tools:$PATH"
```

작업 폴더: `life/calendar_widget` (Gradle 프로젝트 루트가 됨)

---

## Task 1: 프로젝트 뼈대 생성

**Files:**
- Create: `life/calendar_widget/settings.gradle.kts`
- Create: `life/calendar_widget/build.gradle.kts`
- Create: `life/calendar_widget/gradle.properties`
- Create: `life/calendar_widget/gradle/wrapper/gradle-wrapper.properties`
- Create: `life/calendar_widget/app/build.gradle.kts`
- Create: `life/calendar_widget/app/src/main/AndroidManifest.xml`
- Create: `life/calendar_widget/app/src/main/java/com/pulmm/shiftcalendar/MainActivity.kt`
- Create: `life/calendar_widget/app/src/main/java/com/pulmm/shiftcalendar/ShiftCalendarApp.kt`
- Create: `life/calendar_widget/app/src/main/res/values/strings.xml`
- Create: `life/calendar_widget/app/src/main/res/values/themes.xml`
- Create: `life/calendar_widget/app/src/main/res/drawable/ic_launcher_background.xml`
- Create: `life/calendar_widget/app/src/main/res/drawable/ic_launcher_foreground.xml`
- Create: `life/calendar_widget/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- Create: `life/calendar_widget/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- Create: `life/calendar_widget/.gitignore`

- [ ] **Step 1: 루트 Gradle 설정 파일 작성**

`life/calendar_widget/settings.gradle.kts`:
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "ShiftCalendar"
include(":app")
```

`life/calendar_widget/build.gradle.kts`:
```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}
```

`life/calendar_widget/gradle.properties`:
```
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
```

`life/calendar_widget/gradle/wrapper/gradle-wrapper.properties`:
```
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

`life/calendar_widget/.gitignore`:
```
*.iml
.gradle
/local.properties
/.idea
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
local.properties
*.apk
```

- [ ] **Step 2: gradlew 래퍼 스크립트 생성**

```bash
cd "life/calendar_widget"
export JAVA_HOME="/c/dev-tools/jdk-17.0.20.1+1"
/c/dev-tools/gradle-8.9/bin/gradle wrapper --gradle-version 8.9
```
Expected: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` 파일이 생성됨

- [ ] **Step 3: 앱 모듈 build.gradle.kts 작성**

`life/calendar_widget/app/build.gradle.kts`:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.pulmm.shiftcalendar"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.pulmm.shiftcalendar"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("com.github.usingsky:KoreanLunarCalendar:0.4.0")

    testImplementation("junit:junit:4.13.2")
}
```

- [ ] **Step 4: AndroidManifest.xml 작성**

`life/calendar_widget/app/src/main/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

    <application
        android:name=".ShiftCalendarApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:label="@string/app_name"
        android:theme="@style/Theme.ShiftCalendar">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.ShiftCalendar">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>
</manifest>
```

- [ ] **Step 5: 문자열/테마/아이콘 리소스 작성**

`life/calendar_widget/app/src/main/res/values/strings.xml`:
```xml
<resources>
    <string name="app_name">교대캘린더</string>
</resources>
```

`life/calendar_widget/app/src/main/res/values/themes.xml`:
```xml
<resources>
    <style name="Theme.ShiftCalendar" parent="android:Theme.Material.Light.NoActionBar" />

    <style name="Theme.ShiftCalendar.Dialog" parent="android:Theme.Material.Light.Dialog">
        <item name="android:windowIsTranslucent">true</item>
        <item name="android:windowBackground">@android:color/transparent</item>
        <item name="android:windowNoTitle">true</item>
    </style>
</resources>
```

`life/calendar_widget/app/src/main/res/drawable/ic_launcher_background.xml`:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#1E88E5" android:pathData="M0,0h108v108h-108z"/>
</vector>
```

`life/calendar_widget/app/src/main/res/drawable/ic_launcher_foreground.xml`:
```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    <path android:fillColor="#FFFFFF"
        android:pathData="M34,30 h40 a6,6 0 0 1 6,6 v42 a6,6 0 0 1 -6,6 h-40 a6,6 0 0 1 -6,-6 v-42 a6,6 0 0 1 6,-6 z"/>
    <path android:fillColor="#1E88E5" android:pathData="M28,44 h52 v6 h-52 z"/>
    <path android:fillColor="#1E88E5" android:pathData="M42,24 h6 v14 h-6 z"/>
    <path android:fillColor="#1E88E5" android:pathData="M60,24 h6 v14 h-6 z"/>
</vector>
```

`life/calendar_widget/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background"/>
    <foreground android:drawable="@drawable/ic_launcher_foreground"/>
</adaptive-icon>
```

`life/calendar_widget/app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`: 위 파일과 동일한 내용으로 생성

- [ ] **Step 6: ShiftCalendarApp, 빈 MainActivity 작성**

`life/calendar_widget/app/src/main/java/com/pulmm/shiftcalendar/ShiftCalendarApp.kt`:
```kotlin
package com.pulmm.shiftcalendar

import android.app.Application

class ShiftCalendarApp : Application()
```
(Repository는 Task 7에서 이 파일을 수정해 추가한다)

`life/calendar_widget/app/src/main/java/com/pulmm/shiftcalendar/MainActivity.kt`:
```kotlin
package com.pulmm.shiftcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    Text("교대캘린더")
                }
            }
        }
    }
}
```

- [ ] **Step 7: 빌드 확인**

```bash
cd "life/calendar_widget"
export JAVA_HOME="/c/dev-tools/jdk-17.0.20.1+1"
export ANDROID_HOME="/c/Android/sdk"
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 8: 커밋**

```bash
cd "life/calendar_widget"
git add settings.gradle.kts build.gradle.kts gradle.properties gradlew gradlew.bat gradle .gitignore app
git commit -m "프로젝트 뼈대 생성: Gradle 설정, 빈 MainActivity"
```

---

## Task 2: Room 엔티티 + DAO 작성

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/data/entity/ShiftType.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/data/entity/ShiftPattern.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/data/entity/ShiftPatternItem.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/data/entity/DayOverride.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/data/entity/Memo.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/data/dao/ShiftTypeDao.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/data/dao/ShiftPatternDao.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/data/dao/DayOverrideDao.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/data/dao/MemoDao.kt`

(경로는 모두 `life/calendar_widget/` 기준)

- [ ] **Step 1: 엔티티 5개 작성**

`ShiftType.kt`:
```kotlin
package com.pulmm.shiftcalendar.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shift_types")
data class ShiftType(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val colorArgb: Int,
    val sortOrder: Int
)
```

`ShiftPattern.kt`:
```kotlin
package com.pulmm.shiftcalendar.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shift_patterns")
data class ShiftPattern(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val startEpochDay: Long
)
```

`ShiftPatternItem.kt`:
```kotlin
package com.pulmm.shiftcalendar.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shift_pattern_items",
    foreignKeys = [
        ForeignKey(entity = ShiftPattern::class, parentColumns = ["id"], childColumns = ["patternId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = ShiftType::class, parentColumns = ["id"], childColumns = ["shiftTypeId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("patternId"), Index("shiftTypeId")]
)
data class ShiftPatternItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patternId: Long,
    val shiftTypeId: Long,
    val orderIndex: Int
)
```

`DayOverride.kt`:
```kotlin
package com.pulmm.shiftcalendar.data.entity

import androidx.room.Entity

@Entity(tableName = "day_overrides", primaryKeys = ["epochDay"])
data class DayOverride(
    val epochDay: Long,
    val shiftTypeId: Long
)
```

`Memo.kt`:
```kotlin
package com.pulmm.shiftcalendar.data.entity

import androidx.room.Entity

@Entity(tableName = "memos", primaryKeys = ["epochDay"])
data class Memo(
    val epochDay: Long,
    val text: String
)
```

- [ ] **Step 2: DAO 4개 작성**

`ShiftTypeDao.kt`:
```kotlin
package com.pulmm.shiftcalendar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pulmm.shiftcalendar.data.entity.ShiftType
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftTypeDao {
    @Query("SELECT * FROM shift_types ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<ShiftType>>

    @Insert
    suspend fun insert(shiftType: ShiftType): Long

    @Update
    suspend fun update(shiftType: ShiftType)

    @Delete
    suspend fun delete(shiftType: ShiftType)
}
```

`ShiftPatternDao.kt`:
```kotlin
package com.pulmm.shiftcalendar.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import com.pulmm.shiftcalendar.data.entity.ShiftPattern
import com.pulmm.shiftcalendar.data.entity.ShiftPatternItem
import kotlinx.coroutines.flow.Flow

data class PatternWithItems(
    @Embedded val pattern: ShiftPattern,
    @Relation(parentColumn = "id", entityColumn = "patternId")
    val items: List<ShiftPatternItem>
)

@Dao
interface ShiftPatternDao {
    @Transaction
    @Query("SELECT * FROM shift_patterns ORDER BY startEpochDay ASC")
    fun observeAllWithItems(): Flow<List<PatternWithItems>>

    @Insert
    suspend fun insertPattern(pattern: ShiftPattern): Long

    @Insert
    suspend fun insertItems(items: List<ShiftPatternItem>)

    @Delete
    suspend fun deletePattern(pattern: ShiftPattern)
}
```

`DayOverrideDao.kt`:
```kotlin
package com.pulmm.shiftcalendar.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pulmm.shiftcalendar.data.entity.DayOverride
import kotlinx.coroutines.flow.Flow

@Dao
interface DayOverrideDao {
    @Query("SELECT * FROM day_overrides WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay")
    fun observeRange(startEpochDay: Long, endEpochDay: Long): Flow<List<DayOverride>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(override: DayOverride)

    @Query("DELETE FROM day_overrides WHERE epochDay = :epochDay")
    suspend fun delete(epochDay: Long)
}
```

`MemoDao.kt`:
```kotlin
package com.pulmm.shiftcalendar.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pulmm.shiftcalendar.data.entity.Memo
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoDao {
    @Query("SELECT * FROM memos WHERE epochDay BETWEEN :startEpochDay AND :endEpochDay")
    fun observeRange(startEpochDay: Long, endEpochDay: Long): Flow<List<Memo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(memo: Memo)

    @Query("DELETE FROM memos WHERE epochDay = :epochDay")
    suspend fun delete(epochDay: Long)
}
```

- [ ] **Step 3: 빌드 확인**

```bash
cd "life/calendar_widget"
export JAVA_HOME="/c/dev-tools/jdk-17.0.20.1+1"
export ANDROID_HOME="/c/Android/sdk"
./gradlew compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/data
git commit -m "Room 엔티티와 DAO 작성"
```

---

## Task 3: AppDatabase 정의

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/data/AppDatabase.kt`

- [ ] **Step 1: AppDatabase 작성**

```kotlin
package com.pulmm.shiftcalendar.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pulmm.shiftcalendar.data.dao.DayOverrideDao
import com.pulmm.shiftcalendar.data.dao.MemoDao
import com.pulmm.shiftcalendar.data.dao.ShiftPatternDao
import com.pulmm.shiftcalendar.data.dao.ShiftTypeDao
import com.pulmm.shiftcalendar.data.entity.DayOverride
import com.pulmm.shiftcalendar.data.entity.Memo
import com.pulmm.shiftcalendar.data.entity.ShiftPattern
import com.pulmm.shiftcalendar.data.entity.ShiftPatternItem
import com.pulmm.shiftcalendar.data.entity.ShiftType

@Database(
    entities = [ShiftType::class, ShiftPattern::class, ShiftPatternItem::class, DayOverride::class, Memo::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun shiftTypeDao(): ShiftTypeDao
    abstract fun shiftPatternDao(): ShiftPatternDao
    abstract fun dayOverrideDao(): DayOverrideDao
    abstract fun memoDao(): MemoDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "shift_calendar.db"
                ).build().also { instance = it }
            }
    }
}
```

- [ ] **Step 2: 빌드 확인**

```bash
./gradlew compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL` (Room 어노테이션 프로세서가 DAO 구현체를 정상 생성하는지 확인)

- [ ] **Step 3: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/data/AppDatabase.kt
git commit -m "AppDatabase 정의"
```

---

## Task 4: 근무 패턴 반복 계산 로직 (PatternCalculator) — TDD

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/logic/PatternCalculator.kt`
- Test: `app/src/test/java/com/pulmm/shiftcalendar/logic/PatternCalculatorTest.kt`

- [ ] **Step 1: 실패하는 테스트 작성**

`app/src/test/java/com/pulmm/shiftcalendar/logic/PatternCalculatorTest.kt`:
```kotlin
package com.pulmm.shiftcalendar.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PatternCalculatorTest {

    @Test
    fun `패턴이 없으면 null을 반환한다`() {
        val result = PatternCalculator.resolveShiftTypeId(emptyList(), targetEpochDay = 100L)
        assertNull(result)
    }

    @Test
    fun `시작일부터 순서대로 근무를 반복한다`() {
        val pattern = PatternDefinition(startEpochDay = 100L, shiftTypeIdsInOrder = listOf(1L, 2L, 3L))
        assertEquals(1L, PatternCalculator.resolveShiftTypeId(listOf(pattern), 100L))
        assertEquals(2L, PatternCalculator.resolveShiftTypeId(listOf(pattern), 101L))
        assertEquals(3L, PatternCalculator.resolveShiftTypeId(listOf(pattern), 102L))
        assertEquals(1L, PatternCalculator.resolveShiftTypeId(listOf(pattern), 103L))
        assertEquals(3L, PatternCalculator.resolveShiftTypeId(listOf(pattern), 111L))
    }

    @Test
    fun `시작일 이전 날짜는 null을 반환한다`() {
        val pattern = PatternDefinition(startEpochDay = 100L, shiftTypeIdsInOrder = listOf(1L, 2L))
        assertNull(PatternCalculator.resolveShiftTypeId(listOf(pattern), 99L))
    }

    @Test
    fun `가장 최근에 시작한 패턴이 우선 적용된다`() {
        val oldPattern = PatternDefinition(startEpochDay = 100L, shiftTypeIdsInOrder = listOf(1L, 2L))
        val newPattern = PatternDefinition(startEpochDay = 200L, shiftTypeIdsInOrder = listOf(9L))
        val patterns = listOf(oldPattern, newPattern)
        assertEquals(2L, PatternCalculator.resolveShiftTypeId(patterns, 199L))
        assertEquals(9L, PatternCalculator.resolveShiftTypeId(patterns, 200L))
        assertEquals(9L, PatternCalculator.resolveShiftTypeId(patterns, 250L))
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

```bash
./gradlew test --tests "com.pulmm.shiftcalendar.logic.PatternCalculatorTest"
```
Expected: FAIL (PatternCalculator, PatternDefinition이 아직 없음 → 컴파일 오류)

- [ ] **Step 3: PatternCalculator 구현**

`app/src/main/java/com/pulmm/shiftcalendar/logic/PatternCalculator.kt`:
```kotlin
package com.pulmm.shiftcalendar.logic

data class PatternDefinition(
    val startEpochDay: Long,
    val shiftTypeIdsInOrder: List<Long>
)

object PatternCalculator {
    fun resolveShiftTypeId(patterns: List<PatternDefinition>, targetEpochDay: Long): Long? {
        val applicable = patterns
            .filter { it.startEpochDay <= targetEpochDay && it.shiftTypeIdsInOrder.isNotEmpty() }
            .maxByOrNull { it.startEpochDay }
            ?: return null
        val size = applicable.shiftTypeIdsInOrder.size
        val dayOffset = targetEpochDay - applicable.startEpochDay
        val index = ((dayOffset % size) + size) % size
        return applicable.shiftTypeIdsInOrder[index.toInt()]
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

```bash
./gradlew test --tests "com.pulmm.shiftcalendar.logic.PatternCalculatorTest"
```
Expected: `BUILD SUCCESSFUL`, 4개 테스트 모두 통과

- [ ] **Step 5: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/logic/PatternCalculator.kt app/src/test/java/com/pulmm/shiftcalendar/logic/PatternCalculatorTest.kt
git commit -m "근무 패턴 반복 계산 로직 추가 (TDD)"
```

---

## Task 5: 오버라이드 결합 로직 (ShiftResolver) — TDD

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/logic/ShiftResolver.kt`
- Test: `app/src/test/java/com/pulmm/shiftcalendar/logic/ShiftResolverTest.kt`

- [ ] **Step 1: 실패하는 테스트 작성**

```kotlin
package com.pulmm.shiftcalendar.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ShiftResolverTest {

    private val pattern = PatternDefinition(startEpochDay = 100L, shiftTypeIdsInOrder = listOf(1L, 2L))

    @Test
    fun `오버라이드가 없으면 패턴 계산값을 그대로 쓴다`() {
        val result = ShiftResolver.resolveShiftTypeId(listOf(pattern), emptyMap(), 100L)
        assertEquals(1L, result)
    }

    @Test
    fun `오버라이드가 있으면 패턴보다 오버라이드가 우선한다`() {
        val overrides = mapOf(100L to 99L)
        val result = ShiftResolver.resolveShiftTypeId(listOf(pattern), overrides, 100L)
        assertEquals(99L, result)
    }

    @Test
    fun `패턴도 오버라이드도 없으면 null이다`() {
        val result = ShiftResolver.resolveShiftTypeId(emptyList(), emptyMap(), 100L)
        assertNull(result)
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

```bash
./gradlew test --tests "com.pulmm.shiftcalendar.logic.ShiftResolverTest"
```
Expected: FAIL (ShiftResolver 없음)

- [ ] **Step 3: ShiftResolver 구현**

```kotlin
package com.pulmm.shiftcalendar.logic

object ShiftResolver {
    fun resolveShiftTypeId(
        patterns: List<PatternDefinition>,
        overrides: Map<Long, Long>,
        targetEpochDay: Long
    ): Long? {
        overrides[targetEpochDay]?.let { return it }
        return PatternCalculator.resolveShiftTypeId(patterns, targetEpochDay)
    }
}
```

- [ ] **Step 4: 테스트 통과 확인**

```bash
./gradlew test --tests "com.pulmm.shiftcalendar.logic.ShiftResolverTest"
```
Expected: `BUILD SUCCESSFUL`, 3개 테스트 모두 통과

- [ ] **Step 5: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/logic/ShiftResolver.kt app/src/test/java/com/pulmm/shiftcalendar/logic/ShiftResolverTest.kt
git commit -m "오버라이드 우선순위 결합 로직 추가 (TDD)"
```

---

## Task 6: 음력 변환 로직 (LunarConverter) — TDD

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/logic/LunarConverter.kt`
- Test: `app/src/test/java/com/pulmm/shiftcalendar/logic/LunarConverterTest.kt`

이 태스크는 `com.github.usingsky:KoreanLunarCalendar:0.4.0` 라이브러리(오프라인 변환, Task 1에서 의존성 추가됨)를 감싸는 래퍼를 만든다. 테스트 값은 라이브러리 자체 공식 테스트(`KoreanLunarCalendarTest.java`)에서 검증된 값을 그대로 사용한다.

- [ ] **Step 1: 실패하는 테스트 작성**

```kotlin
package com.pulmm.shiftcalendar.logic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class LunarConverterTest {

    @Test
    fun `양력 날짜를 음력으로 변환한다`() {
        val result = LunarConverter.toLunar(LocalDate.of(1956, 3, 3))
        assertEquals(LunarDate(1956, 1, 21, false), result)
    }

    @Test
    fun `윤달인 경우를 구분한다`() {
        val result = LunarConverter.toLunar(LocalDate.of(2017, 6, 24))
        assertEquals(LunarDate(2017, 5, 1, true), result)
    }

    @Test
    fun `지원 범위를 벗어나면 null을 반환한다`() {
        val result = LunarConverter.toLunar(LocalDate.of(2051, 1, 1))
        assertNull(result)
    }

    @Test
    fun `지원 범위의 최댓값 경계를 처리한다`() {
        val result = LunarConverter.toLunar(LocalDate.of(2050, 12, 31))
        assertEquals(LunarDate(2050, 11, 18, false), result)
    }
}
```

- [ ] **Step 2: 테스트 실패 확인**

```bash
./gradlew test --tests "com.pulmm.shiftcalendar.logic.LunarConverterTest"
```
Expected: FAIL (LunarConverter, LunarDate 없음)

- [ ] **Step 3: LunarConverter 구현**

```kotlin
package com.pulmm.shiftcalendar.logic

import com.github.usingsky.calendar.KoreanLunarCalendar
import java.time.LocalDate

data class LunarDate(val year: Int, val month: Int, val day: Int, val isLeapMonth: Boolean) {
    fun toShortDisplay(): String {
        val leapMark = if (isLeapMonth) "윤" else ""
        return "음 $leapMark$month.$day"
    }
}

object LunarConverter {
    @Synchronized
    fun toLunar(date: LocalDate): LunarDate? {
        val calendar = KoreanLunarCalendar.getInstance()
        val ok = calendar.setSolarDate(date.year, date.monthValue, date.dayOfMonth)
        if (!ok) return null
        return LunarDate(
            year = calendar.lunarYear,
            month = calendar.lunarMonth,
            day = calendar.lunarDay,
            isLeapMonth = calendar.isIntercalation
        )
    }
}
```

주의: `KoreanLunarCalendar.getInstance()`는 싱글턴(내부 상태 공유)이므로 `@Synchronized`로 감싸 동시 호출로 인한 값 꼬임을 막는다. 위젯에서 한 달치(최대 42일) 음력을 계산할 때도 반드시 하나씩 순차 호출한다.

- [ ] **Step 4: 테스트 통과 확인**

```bash
./gradlew test --tests "com.pulmm.shiftcalendar.logic.LunarConverterTest"
```
Expected: `BUILD SUCCESSFUL`, 4개 테스트 모두 통과 (최초 실행 시 JitPack에서 라이브러리를 내려받으므로 인터넷 연결 필요 — 런타임에는 인터넷이 필요 없고, 빌드 시 딱 한 번만 다운로드됨)

- [ ] **Step 5: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/logic/LunarConverter.kt app/src/test/java/com/pulmm/shiftcalendar/logic/LunarConverterTest.kt
git commit -m "음력 변환 로직 추가 (TDD)"
```

---

## Task 7: ShiftRepository 작성

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/logic/DayInfo.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/data/DataChangeListener.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/data/ShiftRepository.kt`
- Modify: `app/src/main/java/com/pulmm/shiftcalendar/ShiftCalendarApp.kt`

이 리포지토리는 앱 화면과 위젯이 공유하는 유일한 데이터 접근 창구다. 위젯을 다시 그리는 작업(`WidgetRefresher`, Task 12에서 생성)은 아직 존재하지 않으므로, 지금은 `DataChangeListener`라는 빈 콜백 인터페이스만 두고 실제 위젯 새로고침 연결은 Task 19에서 마무리한다(위젯 클래스가 먼저 있어야 연결할 수 있기 때문).

- [ ] **Step 1: DayInfo 정의**

```kotlin
package com.pulmm.shiftcalendar.logic

data class DayInfo(
    val epochDay: Long,
    val shiftTypeId: Long?,
    val memoText: String?
)
```

- [ ] **Step 2: DataChangeListener 정의**

```kotlin
package com.pulmm.shiftcalendar.data

fun interface DataChangeListener {
    suspend fun onDataChanged()
}
```

- [ ] **Step 3: ShiftRepository 작성**

```kotlin
package com.pulmm.shiftcalendar.data

import com.pulmm.shiftcalendar.data.entity.DayOverride
import com.pulmm.shiftcalendar.data.entity.Memo
import com.pulmm.shiftcalendar.data.entity.ShiftPattern
import com.pulmm.shiftcalendar.data.entity.ShiftPatternItem
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.logic.DayInfo
import com.pulmm.shiftcalendar.logic.PatternDefinition
import com.pulmm.shiftcalendar.logic.ShiftResolver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

class ShiftRepository(
    private val db: AppDatabase,
    private val onDataChanged: DataChangeListener = DataChangeListener {}
) {

    fun observeShiftTypes(): Flow<List<ShiftType>> = db.shiftTypeDao().observeAll()

    fun observePatterns() = db.shiftPatternDao().observeAllWithItems()

    suspend fun getShiftTypesOnce(): List<ShiftType> = observeShiftTypes().first()

    fun observeDayInfoRange(startEpochDay: Long, endEpochDay: Long): Flow<List<DayInfo>> =
        combine(
            db.shiftPatternDao().observeAllWithItems(),
            db.dayOverrideDao().observeRange(startEpochDay, endEpochDay),
            db.memoDao().observeRange(startEpochDay, endEpochDay)
        ) { patternRows, overrides, memos ->
            val patterns = patternRows.map { row ->
                PatternDefinition(
                    startEpochDay = row.pattern.startEpochDay,
                    shiftTypeIdsInOrder = row.items.sortedBy { it.orderIndex }.map { it.shiftTypeId }
                )
            }
            val overrideMap = overrides.associate { it.epochDay to it.shiftTypeId }
            val memoMap = memos.associateBy { it.epochDay }
            (startEpochDay..endEpochDay).map { day ->
                DayInfo(
                    epochDay = day,
                    shiftTypeId = ShiftResolver.resolveShiftTypeId(patterns, overrideMap, day),
                    memoText = memoMap[day]?.text
                )
            }
        }

    suspend fun getDayInfoOnce(epochDay: Long): DayInfo =
        observeDayInfoRange(epochDay, epochDay).first().first()

    suspend fun setOverride(epochDay: Long, shiftTypeId: Long?) {
        if (shiftTypeId == null) db.dayOverrideDao().delete(epochDay)
        else db.dayOverrideDao().upsert(DayOverride(epochDay, shiftTypeId))
        onDataChanged.onDataChanged()
    }

    suspend fun setMemo(epochDay: Long, text: String) {
        if (text.isBlank()) db.memoDao().delete(epochDay) else db.memoDao().upsert(Memo(epochDay, text))
        onDataChanged.onDataChanged()
    }

    suspend fun addShiftType(name: String, colorArgb: Int, sortOrder: Int): Long {
        val id = db.shiftTypeDao().insert(ShiftType(name = name, colorArgb = colorArgb, sortOrder = sortOrder))
        onDataChanged.onDataChanged()
        return id
    }

    suspend fun updateShiftType(shiftType: ShiftType) {
        db.shiftTypeDao().update(shiftType)
        onDataChanged.onDataChanged()
    }

    suspend fun deleteShiftType(shiftType: ShiftType) {
        db.shiftTypeDao().delete(shiftType)
        onDataChanged.onDataChanged()
    }

    suspend fun savePattern(name: String, startEpochDay: Long, shiftTypeIdsInOrder: List<Long>) {
        val patternId = db.shiftPatternDao().insertPattern(ShiftPattern(name = name, startEpochDay = startEpochDay))
        db.shiftPatternDao().insertItems(
            shiftTypeIdsInOrder.mapIndexed { index, shiftTypeId ->
                ShiftPatternItem(patternId = patternId, shiftTypeId = shiftTypeId, orderIndex = index)
            }
        )
        onDataChanged.onDataChanged()
    }

    suspend fun deletePattern(pattern: ShiftPattern) {
        db.shiftPatternDao().deletePattern(pattern)
        onDataChanged.onDataChanged()
    }
}
```

- [ ] **Step 4: ShiftCalendarApp에 repository 추가**

`app/src/main/java/com/pulmm/shiftcalendar/ShiftCalendarApp.kt` 전체를 아래 내용으로 교체:
```kotlin
package com.pulmm.shiftcalendar

import android.app.Application
import com.pulmm.shiftcalendar.data.AppDatabase
import com.pulmm.shiftcalendar.data.ShiftRepository

class ShiftCalendarApp : Application() {
    val repository: ShiftRepository by lazy {
        ShiftRepository(AppDatabase.getInstance(this))
    }
}
```
(위젯 새로고침 콜백은 Task 19에서 두 번째 생성자 인자로 연결한다)

- [ ] **Step 5: 빌드 확인**

```bash
./gradlew compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/logic/DayInfo.kt app/src/main/java/com/pulmm/shiftcalendar/data/DataChangeListener.kt app/src/main/java/com/pulmm/shiftcalendar/data/ShiftRepository.kt app/src/main/java/com/pulmm/shiftcalendar/ShiftCalendarApp.kt
git commit -m "ShiftRepository 작성: DB와 계산 로직 결합"
```

---

## Task 8: 근무 종류 관리 화면

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/ui/RepositoryProvider.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/ui/shifttype/ShiftTypeViewModel.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/ui/shifttype/ShiftTypeScreen.kt`

- [ ] **Step 1: Composable에서 repository를 가져오는 공용 헬퍼 작성**

```kotlin
package com.pulmm.shiftcalendar.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.pulmm.shiftcalendar.ShiftCalendarApp
import com.pulmm.shiftcalendar.data.ShiftRepository

@Composable
fun rememberRepository(): ShiftRepository {
    val context = LocalContext.current
    return (context.applicationContext as ShiftCalendarApp).repository
}
```

- [ ] **Step 2: ShiftTypeViewModel 작성**

```kotlin
package com.pulmm.shiftcalendar.ui.shifttype

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulmm.shiftcalendar.data.ShiftRepository
import com.pulmm.shiftcalendar.data.entity.ShiftType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShiftTypeViewModel(private val repository: ShiftRepository) : ViewModel() {

    val shiftTypes: StateFlow<List<ShiftType>> = repository.observeShiftTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addShiftType(name: String, colorArgb: Int) {
        viewModelScope.launch { repository.addShiftType(name, colorArgb, shiftTypes.value.size) }
    }

    fun deleteShiftType(shiftType: ShiftType) {
        viewModelScope.launch { repository.deleteShiftType(shiftType) }
    }

    class Factory(private val repository: ShiftRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ShiftTypeViewModel(repository) as T
    }
}
```

- [ ] **Step 3: ShiftTypeScreen 작성**

```kotlin
package com.pulmm.shiftcalendar.ui.shifttype

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.ui.rememberRepository

private val PALETTE = listOf(
    0xFF4A90D9.toInt(), 0xFF8E44AD.toInt(), 0xFF27AE60.toInt(),
    0xFFE67E22.toInt(), 0xFFE74C3C.toInt(), 0xFF7F8C8D.toInt(),
    0xFF2C3E50.toInt(), 0xFFF1C40F.toInt()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShiftTypeScreen() {
    val viewModel: ShiftTypeViewModel = viewModel(factory = ShiftTypeViewModel.Factory(rememberRepository()))
    val shiftTypes by viewModel.shiftTypes.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("근무 종류 관리") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) { Text("+") }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(shiftTypes, key = { it.id }) { shiftType ->
                ShiftTypeRow(shiftType = shiftType, onDelete = { viewModel.deleteShiftType(shiftType) })
            }
        }
    }

    if (showAddDialog) {
        AddShiftTypeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color ->
                viewModel.addShiftType(name, color)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun ShiftTypeRow(shiftType: ShiftType, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(shiftType.colorArgb))
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(shiftType.name, modifier = Modifier.weight(1f, fill = true))
        TextButton(onClick = onDelete) { Text("삭제") }
    }
}

@Composable
private fun AddShiftTypeDialog(onDismiss: () -> Unit, onConfirm: (String, Int) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(PALETTE.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("근무 종류 추가") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("이름 (예: 주간)") })
                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    PALETTE.forEach { colorInt ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .clickable { selectedColor = colorInt }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, selectedColor) }, enabled = name.isNotBlank()) {
                Text("추가")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )
}
```

- [ ] **Step 4: 빌드 확인**

```bash
./gradlew compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/ui/RepositoryProvider.kt app/src/main/java/com/pulmm/shiftcalendar/ui/shifttype
git commit -m "근무 종류 관리 화면 추가"
```

---

## Task 9: 근무 패턴 관리 화면

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/ui/pattern/PatternViewModel.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/ui/pattern/PatternScreen.kt`

- [ ] **Step 1: PatternViewModel 작성**

```kotlin
package com.pulmm.shiftcalendar.ui.pattern

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulmm.shiftcalendar.data.ShiftRepository
import com.pulmm.shiftcalendar.data.dao.PatternWithItems
import com.pulmm.shiftcalendar.data.entity.ShiftType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class PatternViewModel(private val repository: ShiftRepository) : ViewModel() {

    val shiftTypes: StateFlow<List<ShiftType>> = repository.observeShiftTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val patterns: StateFlow<List<PatternWithItems>> = repository.observePatterns()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun savePattern(name: String, startDate: LocalDate, shiftTypeIdsInOrder: List<Long>) {
        viewModelScope.launch { repository.savePattern(name, startDate.toEpochDay(), shiftTypeIdsInOrder) }
    }

    fun deletePattern(patternWithItems: PatternWithItems) {
        viewModelScope.launch { repository.deletePattern(patternWithItems.pattern) }
    }

    class Factory(private val repository: ShiftRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PatternViewModel(repository) as T
    }
}
```

- [ ] **Step 2: PatternScreen 작성**

```kotlin
package com.pulmm.shiftcalendar.ui.pattern

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulmm.shiftcalendar.data.dao.PatternWithItems
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.ui.rememberRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatternScreen() {
    val viewModel: PatternViewModel = viewModel(factory = PatternViewModel.Factory(rememberRepository()))
    val shiftTypes by viewModel.shiftTypes.collectAsState()
    val patterns by viewModel.patterns.collectAsState()
    val shiftTypeById = remember(shiftTypes) { shiftTypes.associateBy { it.id } }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("근무 패턴 관리") }) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }) { Text("+") } }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
            items(patterns, key = { it.pattern.id }) { patternWithItems ->
                PatternRow(
                    patternWithItems = patternWithItems,
                    shiftTypeById = shiftTypeById,
                    onDelete = { viewModel.deletePattern(patternWithItems) }
                )
            }
        }
    }

    if (showAddDialog) {
        AddPatternDialog(
            shiftTypes = shiftTypes,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, startDate, order ->
                viewModel.savePattern(name, startDate, order)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PatternRow(patternWithItems: PatternWithItems, shiftTypeById: Map<Long, ShiftType>, onDelete: () -> Unit) {
    val startDate = LocalDate.ofEpochDay(patternWithItems.pattern.startEpochDay)
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f, fill = true)) {
                Text(patternWithItems.pattern.name, style = MaterialTheme.typography.titleMedium)
                Text("시작일: ${startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}", style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onDelete) { Text("삭제") }
        }
        Row(modifier = Modifier.padding(top = 8.dp)) {
            patternWithItems.items.sortedBy { it.orderIndex }.forEach { item ->
                val shiftType = shiftTypeById[item.shiftTypeId]
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(shiftType?.colorArgb?.let { Color(it) } ?: Color.Gray)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(shiftType?.name ?: "삭제됨", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPatternDialog(
    shiftTypes: List<ShiftType>,
    onDismiss: () -> Unit,
    onConfirm: (String, LocalDate, List<Long>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var order by remember { mutableStateOf(listOf<Long>()) }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("근무 패턴 추가") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("패턴 이름") })
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { showDatePicker = true }) {
                    Text("시작일: ${startDate.format(DateTimeFormatter.ISO_LOCAL_DATE)}")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text("순서대로 근무 종류를 탭해서 추가하세요", style = MaterialTheme.typography.bodySmall)
                LazyRow(modifier = Modifier.padding(vertical = 8.dp)) {
                    items(shiftTypes, key = { it.id }) { shiftType ->
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(shiftType.colorArgb))
                                .clickable { order = order + shiftType.id }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(shiftType.name, color = Color.White)
                        }
                    }
                }
                Text("현재 순서: " + order.joinToString(" → ") { id -> shiftTypes.find { it.id == id }?.name ?: "?" })
                TextButton(onClick = { order = emptyList() }) { Text("순서 초기화") }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank() && order.isNotEmpty()) onConfirm(name, startDate, order) },
                enabled = name.isNotBlank() && order.isNotEmpty()
            ) { Text("저장") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    )

    if (showDatePicker) {
        StartDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onConfirm = { date -> startDate = date; showDatePicker = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartDatePickerDialog(onDismiss: () -> Unit, onConfirm: (LocalDate) -> Unit) {
    val state = rememberDatePickerState()
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = state.selectedDateMillis
                if (millis != null) onConfirm(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
            }) { Text("확인") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("취소") } }
    ) {
        DatePicker(state = state)
    }
}
```

- [ ] **Step 3: 빌드 확인**

```bash
./gradlew compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 4: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/ui/pattern
git commit -m "근무 패턴 관리 화면 추가"
```

---

## Task 10: 달력(홈) 화면 + 날짜 편집 바텀시트

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/ui/calendar/CalendarViewModel.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/ui/calendar/CalendarScreen.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/ui/calendar/DayEditSheet.kt`

- [ ] **Step 1: CalendarViewModel 작성**

```kotlin
package com.pulmm.shiftcalendar.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.pulmm.shiftcalendar.data.ShiftRepository
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.logic.DayInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(private val repository: ShiftRepository) : ViewModel() {

    private val visibleMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = visibleMonth

    val shiftTypes: StateFlow<List<ShiftType>> = repository.observeShiftTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val monthDayInfos: StateFlow<List<DayInfo>> = visibleMonth
        .flatMapLatest { month ->
            repository.observeDayInfoRange(month.atDay(1).toEpochDay(), month.atEndOfMonth().toEpochDay())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun goToPreviousMonth() { visibleMonth.value = visibleMonth.value.minusMonths(1) }
    fun goToNextMonth() { visibleMonth.value = visibleMonth.value.plusMonths(1) }

    fun setOverride(date: LocalDate, shiftTypeId: Long?) {
        viewModelScope.launch { repository.setOverride(date.toEpochDay(), shiftTypeId) }
    }

    fun setMemo(date: LocalDate, text: String) {
        viewModelScope.launch { repository.setMemo(date.toEpochDay(), text) }
    }

    class Factory(private val repository: ShiftRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = CalendarViewModel(repository) as T
    }
}
```

- [ ] **Step 2: DayEditSheet 작성**

```kotlin
package com.pulmm.shiftcalendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.logic.DayInfo
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayEditSheet(
    date: LocalDate,
    dayInfo: DayInfo?,
    shiftTypes: List<ShiftType>,
    onDismiss: () -> Unit,
    onSelectShiftType: (Long) -> Unit,
    onClearShiftType: () -> Unit,
    onSaveMemo: (String) -> Unit
) {
    var memoText by remember(date) { mutableStateOf(dayInfo?.memoText ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(date.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일")), style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(12.dp))
            Text("근무 선택", style = MaterialTheme.typography.labelLarge)
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                shiftTypes.forEach { shiftType ->
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(shiftType.colorArgb))
                            .clickable { onSelectShiftType(shiftType.id) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(shiftType.name, color = Color.White)
                    }
                }
            }
            TextButton(onClick = onClearShiftType) { Text("이 날짜를 패턴 기본값으로 되돌리기") }
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = memoText,
                onValueChange = { memoText = it },
                label = { Text("메모") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = { onSaveMemo(memoText); onDismiss() }, modifier = Modifier.fillMaxWidth()) { Text("저장") }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
```

- [ ] **Step 3: CalendarScreen 작성**

```kotlin
package com.pulmm.shiftcalendar.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.logic.DayInfo
import com.pulmm.shiftcalendar.ui.rememberRepository
import java.time.LocalDate
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val viewModel: CalendarViewModel = viewModel(factory = CalendarViewModel.Factory(rememberRepository()))
    val month by viewModel.currentMonth.collectAsState()
    val dayInfos by viewModel.monthDayInfos.collectAsState()
    val shiftTypes by viewModel.shiftTypes.collectAsState()
    val shiftTypeById = remember(shiftTypes) { shiftTypes.associateBy { it.id } }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("${month.year}년 ${month.monthValue}월") },
            navigationIcon = {
                IconButton(onClick = viewModel::goToPreviousMonth) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "이전 달")
                }
            },
            actions = {
                IconButton(onClick = viewModel::goToNextMonth) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "다음 달")
                }
            }
        )
    }) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            WeekdayHeaderRow()
            MonthGrid(
                month = month,
                dayInfoByEpochDay = remember(dayInfos) { dayInfos.associateBy { it.epochDay } },
                shiftTypeById = shiftTypeById,
                onDayClick = { date -> selectedDate = date }
            )
        }
    }

    selectedDate?.let { date ->
        val dayInfo = dayInfos.find { it.epochDay == date.toEpochDay() }
        DayEditSheet(
            date = date,
            dayInfo = dayInfo,
            shiftTypes = shiftTypes,
            onDismiss = { selectedDate = null },
            onSelectShiftType = { shiftTypeId -> viewModel.setOverride(date, shiftTypeId) },
            onClearShiftType = { viewModel.setOverride(date, null) },
            onSaveMemo = { text -> viewModel.setMemo(date, text) }
        )
    }
}

@Composable
private fun WeekdayHeaderRow() {
    val labels = listOf("일", "월", "화", "수", "목", "금", "토")
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        labels.forEachIndexed { index, label ->
            val color = when (index) { 0 -> Color.Red; 6 -> Color.Blue; else -> Color.Gray }
            Text(label, color = color, modifier = Modifier.weight(1f, fill = true), textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    dayInfoByEpochDay: Map<Long, DayInfo>,
    shiftTypeById: Map<Long, ShiftType>,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = month.atDay(1)
    val leadingBlanks = firstDayOfMonth.dayOfWeek.value % 7
    val totalDays = month.lengthOfMonth()
    val cells: List<LocalDate?> = List(leadingBlanks) { null } + (1..totalDays).map { month.atDay(it) }
    val weeks = cells.chunked(7)
    val today = LocalDate.now()

    Column {
        weeks.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(
                        modifier = Modifier
                            .weight(1f, fill = true)
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .then(
                                if (date == today) Modifier.background(Color(0xFFFFF3CD), RoundedCornerShape(6.dp))
                                else Modifier
                            )
                            .clickable(enabled = date != null) { date?.let(onDayClick) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (date != null) {
                            val dayInfo = dayInfoByEpochDay[date.toEpochDay()]
                            val shiftType = dayInfo?.shiftTypeId?.let { shiftTypeById[it] }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${date.dayOfMonth}", fontWeight = if (date == today) FontWeight.Bold else FontWeight.Normal)
                                if (shiftType != null) {
                                    Text(shiftType.name, style = MaterialTheme.typography.labelSmall, color = Color(shiftType.colorArgb))
                                }
                                if (dayInfo?.memoText?.isNotBlank() == true) {
                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(Color(0xFFF1C40F)))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 4: 빌드 확인**

```bash
./gradlew compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/ui/calendar
git commit -m "달력 홈 화면과 날짜 편집 바텀시트 추가"
```

---

## Task 11: 내비게이션 연결 + 위젯 추가 안내 화면 + 앱 빌드/실행 확인

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/ui/widgethelp/WidgetHelpScreen.kt`
- Modify: `app/src/main/java/com/pulmm/shiftcalendar/MainActivity.kt`

- [ ] **Step 1: WidgetHelpScreen 작성**

```kotlin
package com.pulmm.shiftcalendar.ui.widgethelp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WidgetHelpScreen() {
    Scaffold(topBar = { TopAppBar(title = { Text("위젯 추가 방법") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
            Text(
                "홈 화면의 빈 곳을 길게 눌러 '위젯' 메뉴로 들어간 뒤, '교대캘린더'를 찾아 아래 4종류 중 원하는 크기를 골라 추가하세요.",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            listOf(
                "한달위젯" to "한 달 전체 달력, 4x4 크기",
                "작은한달위젯" to "한 달을 더 작게, 3x2 크기",
                "일주일위젯" to "이번 주 7일, 가로로 긴 4x1 크기",
                "하루위젯" to "오늘 하루만, 가장 작은 1x1 크기"
            ).forEach { (title, desc) ->
                ListItem(headlineContent = { Text(title) }, supportingContent = { Text(desc) })
            }
        }
    }
}
```

- [ ] **Step 2: MainActivity를 하단 내비게이션으로 교체**

`MainActivity.kt` 전체 교체:
```kotlin
package com.pulmm.shiftcalendar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.pulmm.shiftcalendar.ui.calendar.CalendarScreen
import com.pulmm.shiftcalendar.ui.pattern.PatternScreen
import com.pulmm.shiftcalendar.ui.shifttype.ShiftTypeScreen
import com.pulmm.shiftcalendar.ui.widgethelp.WidgetHelpScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme { MainScreen() }
        }
    }
}

private enum class Tab(val label: String) { CALENDAR("달력"), SHIFT_TYPE("근무종류"), PATTERN("패턴"), WIDGET_HELP("위젯추가") }

@Composable
private fun MainScreen() {
    var selectedTab by remember { mutableStateOf(Tab.CALENDAR) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == Tab.CALENDAR,
                    onClick = { selectedTab = Tab.CALENDAR },
                    icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                    label = { Text(Tab.CALENDAR.label) }
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.SHIFT_TYPE,
                    onClick = { selectedTab = Tab.SHIFT_TYPE },
                    icon = { Icon(Icons.Default.Category, contentDescription = null) },
                    label = { Text(Tab.SHIFT_TYPE.label) }
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.PATTERN,
                    onClick = { selectedTab = Tab.PATTERN },
                    icon = { Icon(Icons.Default.ViewList, contentDescription = null) },
                    label = { Text(Tab.PATTERN.label) }
                )
                NavigationBarItem(
                    selected = selectedTab == Tab.WIDGET_HELP,
                    onClick = { selectedTab = Tab.WIDGET_HELP },
                    icon = { Icon(Icons.Default.Widgets, contentDescription = null) },
                    label = { Text(Tab.WIDGET_HELP.label) }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                Tab.CALENDAR -> CalendarScreen()
                Tab.SHIFT_TYPE -> ShiftTypeScreen()
                Tab.PATTERN -> PatternScreen()
                Tab.WIDGET_HELP -> WidgetHelpScreen()
            }
        }
    }
}
```

- [ ] **Step 3: 빌드 확인**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`. 이 시점부터는 휴대폰이나 에뮬레이터에 `app/build/outputs/apk/debug/app-debug.apk`를 설치해 실제로 근무 종류 추가 → 패턴 등록 → 달력에서 확인하는 과정을 눈으로 확인할 수 있다.

- [ ] **Step 4: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/ui/widgethelp app/src/main/java/com/pulmm/shiftcalendar/MainActivity.kt
git commit -m "하단 내비게이션 연결, 위젯 추가 안내 화면 추가"
```

---

## Task 12: 위젯 공통 인프라

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/common/WidgetSettingsKeys.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/common/WidgetStyle.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/common/WidgetUi.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/common/MemoEditActivity.kt`

`androidx.datastore.preferences.core.*` (Preferences, key 타입들)는 `androidx.glance:glance-appwidget`이 내부적으로 의존하는 라이브러리라 별도 의존성 추가 없이 바로 사용할 수 있다.

- [ ] **Step 1: 설정 키 정의**

```kotlin
package com.pulmm.shiftcalendar.widget.common

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

object WidgetSettingsKeys {
    val BG_COLOR_ARGB = intPreferencesKey("bg_color_argb")
    val OPACITY = floatPreferencesKey("opacity")
    val DATE_FONT_SCALE = floatPreferencesKey("date_font_scale")
    val SHIFT_FONT_SCALE = floatPreferencesKey("shift_font_scale")
    val MEMO_FONT_SCALE = floatPreferencesKey("memo_font_scale")
    val SHOW_LUNAR = booleanPreferencesKey("show_lunar")
    val WEEK_START_MONDAY = booleanPreferencesKey("week_start_monday")
    val MONTH_OFFSET = intPreferencesKey("month_offset")
}
```

- [ ] **Step 2: WidgetStyle 정의**

```kotlin
package com.pulmm.shiftcalendar.widget.common

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences

data class WidgetStyle(
    val bgColorArgb: Int,
    val opacity: Float,
    val dateFontScale: Float,
    val shiftFontScale: Float,
    val memoFontScale: Float,
    val showLunar: Boolean,
    val weekStartMonday: Boolean
)

val DEFAULT_WIDGET_STYLE = WidgetStyle(
    bgColorArgb = 0xFF2A2A2A.toInt(),
    opacity = 1f,
    dateFontScale = 1f,
    shiftFontScale = 1f,
    memoFontScale = 1f,
    showLunar = false,
    weekStartMonday = false
)

fun Preferences.toWidgetStyle(): WidgetStyle = WidgetStyle(
    bgColorArgb = this[WidgetSettingsKeys.BG_COLOR_ARGB] ?: DEFAULT_WIDGET_STYLE.bgColorArgb,
    opacity = this[WidgetSettingsKeys.OPACITY] ?: DEFAULT_WIDGET_STYLE.opacity,
    dateFontScale = this[WidgetSettingsKeys.DATE_FONT_SCALE] ?: DEFAULT_WIDGET_STYLE.dateFontScale,
    shiftFontScale = this[WidgetSettingsKeys.SHIFT_FONT_SCALE] ?: DEFAULT_WIDGET_STYLE.shiftFontScale,
    memoFontScale = this[WidgetSettingsKeys.MEMO_FONT_SCALE] ?: DEFAULT_WIDGET_STYLE.memoFontScale,
    showLunar = this[WidgetSettingsKeys.SHOW_LUNAR] ?: DEFAULT_WIDGET_STYLE.showLunar,
    weekStartMonday = this[WidgetSettingsKeys.WEEK_START_MONDAY] ?: DEFAULT_WIDGET_STYLE.weekStartMonday
)

fun MutablePreferences.applyStyle(style: WidgetStyle): MutablePreferences = apply {
    this[WidgetSettingsKeys.BG_COLOR_ARGB] = style.bgColorArgb
    this[WidgetSettingsKeys.OPACITY] = style.opacity
    this[WidgetSettingsKeys.DATE_FONT_SCALE] = style.dateFontScale
    this[WidgetSettingsKeys.SHIFT_FONT_SCALE] = style.shiftFontScale
    this[WidgetSettingsKeys.MEMO_FONT_SCALE] = style.memoFontScale
    this[WidgetSettingsKeys.SHOW_LUNAR] = style.showLunar
    this[WidgetSettingsKeys.WEEK_START_MONDAY] = style.weekStartMonday
}
```

- [ ] **Step 3: 공통 Glance UI 조각 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pulmm.shiftcalendar.data.entity.ShiftType
import com.pulmm.shiftcalendar.logic.DayInfo
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun WidgetRoot(style: WidgetStyle, content: @Composable () -> Unit) {
    val bg = Color(style.bgColorArgb).copy(alpha = style.opacity)
    Box(modifier = GlanceModifier.fillMaxSize().background(ColorProvider(bg)).padding(6.dp)) {
        content()
    }
}

@Composable
fun WeekdayHeaderRow(weekStartMonday: Boolean, fontScale: Float) {
    val labels = if (weekStartMonday) listOf("월", "화", "수", "목", "금", "토", "일")
    else listOf("일", "월", "화", "수", "목", "금", "토")
    Row(modifier = GlanceModifier.fillMaxWidth()) {
        labels.forEachIndexed { index, label ->
            val isSunday = if (weekStartMonday) index == 6 else index == 0
            val isSaturday = if (weekStartMonday) index == 5 else index == 6
            val color = when {
                isSunday -> Color(0xFFE74C3C)
                isSaturday -> Color(0xFF3498DB)
                else -> Color(0xFFAAAAAA)
            }
            Box(modifier = GlanceModifier.defaultWeight()) {
                Text(label, style = TextStyle(color = ColorProvider(color), fontSize = (10 * fontScale).sp))
            }
        }
    }
}

fun buildMonthGrid(month: YearMonth, weekStartMonday: Boolean): List<List<LocalDate?>> {
    val firstDay = month.atDay(1)
    val firstDayIndex = if (weekStartMonday) (firstDay.dayOfWeek.value + 6) % 7 else firstDay.dayOfWeek.value % 7
    val totalDays = month.lengthOfMonth()
    val cells = List(firstDayIndex) { null } + (1..totalDays).map { month.atDay(it) }
    val paddedSize = ((cells.size + 6) / 7) * 7
    val padded = cells + List(paddedSize - cells.size) { null }
    return padded.chunked(7)
}

@Composable
fun MonthGridView(
    weeks: List<List<LocalDate?>>,
    dayInfoByEpochDay: Map<Long, DayInfo>,
    shiftTypeById: Map<Long, ShiftType>,
    style: WidgetStyle,
    showShiftName: Boolean
) {
    val today = LocalDate.now()
    Column {
        weeks.forEach { week ->
            Row(modifier = GlanceModifier.fillMaxWidth()) {
                week.forEach { date ->
                    Box(modifier = GlanceModifier.defaultWeight()) {
                        if (date != null) {
                            MonthDayCell(date, dayInfoByEpochDay[date.toEpochDay()], shiftTypeById, style, showShiftName, date == today)
                        } else {
                            Spacer(modifier = GlanceModifier.height(1.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthDayCell(
    date: LocalDate,
    dayInfo: DayInfo?,
    shiftTypeById: Map<Long, ShiftType>,
    style: WidgetStyle,
    showShiftName: Boolean,
    isToday: Boolean
) {
    val shiftType = dayInfo?.shiftTypeId?.let { shiftTypeById[it] }
    Column(
        modifier = GlanceModifier
            .padding(1.dp)
            .clickable(actionStartActivity<MemoEditActivity>(
                actionParametersOf(MemoEditActivity.EPOCH_DAY_KEY to date.toEpochDay())
            ))
    ) {
        Text(
            "${date.dayOfMonth}",
            style = TextStyle(
                fontSize = (10 * style.dateFontScale).sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = ColorProvider(Color(0xFFEEEEEE))
            )
        )
        if (showShiftName && shiftType != null) {
            Text(
                shiftType.name,
                style = TextStyle(fontSize = (8 * style.shiftFontScale).sp, color = ColorProvider(Color(shiftType.colorArgb)))
            )
        }
        if (shiftType != null) {
            Box(modifier = GlanceModifier.fillMaxWidth().height(2.dp).background(ColorProvider(Color(shiftType.colorArgb))))
        }
        if (dayInfo?.memoText?.isNotBlank() == true) {
            Box(modifier = GlanceModifier.height(3.dp).background(ColorProvider(Color(0xFFF1C40F))))
        }
    }
}
```

- [ ] **Step 4: 위젯 탭 메모 편집 액티비티 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.common

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.glance.action.ActionParameters
import com.pulmm.shiftcalendar.ShiftCalendarApp
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MemoEditActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val epochDay = intent.getLongExtra(EPOCH_DAY_EXTRA, LocalDate.now().toEpochDay())
        val repository = (application as ShiftCalendarApp).repository

        setContent {
            val date = remember { LocalDate.ofEpochDay(epochDay) }
            var text by remember { mutableStateOf("") }
            val scope = rememberCoroutineScope()

            LaunchedEffect(epochDay) {
                text = repository.getDayInfoOnce(epochDay).memoText ?: ""
            }

            MaterialTheme {
                AlertDialog(
                    onDismissRequest = { finish() },
                    title = { Text(date.format(DateTimeFormatter.ofPattern("yyyy년 M월 d일")) + " 메모") },
                    text = {
                        OutlinedTextField(value = text, onValueChange = { text = it }, modifier = Modifier.fillMaxWidth())
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            scope.launch {
                                repository.setMemo(epochDay, text)
                                finish()
                            }
                        }) { Text("저장") }
                    },
                    dismissButton = { TextButton(onClick = { finish() }) { Text("취소") } }
                )
            }
        }
    }

    companion object {
        const val EPOCH_DAY_EXTRA = "epoch_day"
        val EPOCH_DAY_KEY = ActionParameters.Key<Long>(EPOCH_DAY_EXTRA)
    }
}
```

- [ ] **Step 5: 빌드 확인**

```bash
./gradlew compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/widget/common
git commit -m "위젯 공통 인프라 작성: 설정 키, 스타일, 공통 UI, 메모 편집 액티비티"
```

---

## Task 13: 위젯 공통 구성(설정) 화면

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/common/WidgetConfigContent.kt`

- [ ] **Step 1: 공통 설정 화면 Composable 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val BG_PALETTE = listOf(
    0xFF2A2A2A.toInt(), 0xFFFFFFFF.toInt(), 0xFF1E3A5F.toInt(),
    0xFF4A2E2A.toInt(), 0xFF2E4A2E.toInt(), 0xFF3A2E4A.toInt()
)

@Composable
fun WidgetConfigContent(initialStyle: WidgetStyle, showLunarOption: Boolean, onSave: (WidgetStyle) -> Unit) {
    var bgColor by remember { mutableStateOf(initialStyle.bgColorArgb) }
    var opacity by remember { mutableStateOf(initialStyle.opacity) }
    var dateFontScale by remember { mutableStateOf(initialStyle.dateFontScale) }
    var shiftFontScale by remember { mutableStateOf(initialStyle.shiftFontScale) }
    var memoFontScale by remember { mutableStateOf(initialStyle.memoFontScale) }
    var showLunar by remember { mutableStateOf(initialStyle.showLunar) }
    var weekStartMonday by remember { mutableStateOf(initialStyle.weekStartMonday) }

    MaterialTheme {
        Scaffold(
            topBar = { TopAppBar(title = { Text("위젯 디자인 설정") }) },
            bottomBar = {
                Button(
                    onClick = {
                        onSave(WidgetStyle(bgColor, opacity, dateFontScale, shiftFontScale, memoFontScale, showLunar, weekStartMonday))
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) { Text("완료") }
            }
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp).fillMaxSize()) {
                Text("배경색")
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    BG_PALETTE.forEach { colorInt ->
                        androidx.compose.foundation.layout.Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorInt))
                                .clickable { bgColor = colorInt }
                        )
                    }
                }
                Text("배경 투명도: ${(opacity * 100).toInt()}%")
                Slider(value = opacity, onValueChange = { opacity = it }, valueRange = 0.2f..1f)

                Text("날짜/요일 글자 크기: ${"%.1f".format(dateFontScale)}배")
                Slider(value = dateFontScale, onValueChange = { dateFontScale = it }, valueRange = 0.7f..1.6f)

                Text("근무 이름 글자 크기: ${"%.1f".format(shiftFontScale)}배")
                Slider(value = shiftFontScale, onValueChange = { shiftFontScale = it }, valueRange = 0.7f..1.6f)

                Text("메모 글자 크기: ${"%.1f".format(memoFontScale)}배")
                Slider(value = memoFontScale, onValueChange = { memoFontScale = it }, valueRange = 0.7f..1.6f)

                if (showLunarOption) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Text("음력 표시", modifier = Modifier.weight(1f, fill = true))
                        Switch(checked = showLunar, onCheckedChange = { showLunar = it })
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                    Text("월요일부터 시작", modifier = Modifier.weight(1f, fill = true))
                    Switch(checked = weekStartMonday, onCheckedChange = { weekStartMonday = it })
                }
            }
        }
    }
}
```

- [ ] **Step 2: 빌드 확인**

```bash
./gradlew compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/widget/common/WidgetConfigContent.kt
git commit -m "위젯 공통 구성 화면 추가"
```

---

## Task 14: 위젯 자정 자동 갱신 스케줄러

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/common/WidgetAlarmScheduler.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/common/MidnightRefreshReceiver.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/common/BootCompletedReceiver.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/common/WidgetRefresher.kt`
- Modify: `app/src/main/AndroidManifest.xml`

`WidgetRefresher`는 4개 위젯 클래스를 참조하므로, 실제 내용은 4개 위젯을 모두 만든 뒤(Task 18 이후) 채운다. 지금은 컴파일되는 빈 형태로만 만든다.

- [ ] **Step 1: WidgetRefresher (임시 빈 구현) 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.common

import android.content.Context

object WidgetRefresher {
    suspend fun refreshAll(context: Context) {
        // Task 19에서 하루/일주일/작은한달/한달위젯 updateAll 호출로 채운다
    }
}
```

- [ ] **Step 2: 알람 스케줄러 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.common

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.LocalDate
import java.time.ZoneId

object WidgetAlarmScheduler {
    fun scheduleNextMidnight(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val nextMidnight = LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault())
        val triggerAtMillis = nextMidnight.toInstant().toEpochMilli()

        val intent = Intent(context, MidnightRefreshReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
    }
}
```
(`setAndAllowWhileIdle`을 사용해 안드로이드 12+의 "정확한 알람" 특별 권한 없이도 자정 무렵 자동 갱신되게 한다. 몇 분 오차는 캘린더 위젯 용도에 문제없다)

- [ ] **Step 3: 자정 갱신 리시버 + 부팅 후 재예약 리시버 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MidnightRefreshReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                WidgetRefresher.refreshAll(context)
            } finally {
                WidgetAlarmScheduler.scheduleNextMidnight(context)
                pendingResult.finish()
            }
        }
    }
}
```

```kotlin
package com.pulmm.shiftcalendar.widget.common

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            WidgetAlarmScheduler.scheduleNextMidnight(context)
        }
    }
}
```

- [ ] **Step 4: ShiftCalendarApp에서 최초 알람 예약, 매니페스트에 리시버 등록**

`ShiftCalendarApp.kt`의 `onCreate`를 override해서 최초 알람을 예약하도록 수정:
```kotlin
package com.pulmm.shiftcalendar

import android.app.Application
import com.pulmm.shiftcalendar.data.AppDatabase
import com.pulmm.shiftcalendar.data.ShiftRepository
import com.pulmm.shiftcalendar.widget.common.WidgetAlarmScheduler

class ShiftCalendarApp : Application() {
    val repository: ShiftRepository by lazy {
        ShiftRepository(AppDatabase.getInstance(this))
    }

    override fun onCreate() {
        super.onCreate()
        WidgetAlarmScheduler.scheduleNextMidnight(this)
    }
}
```

`AndroidManifest.xml`의 `<application>` 태그 안, `</application>` 바로 앞에 추가:
```xml
<receiver android:name=".widget.common.MidnightRefreshReceiver" android:exported="false" />
<receiver android:name=".widget.common.BootCompletedReceiver" android:exported="false">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
    </intent-filter>
</receiver>
```

- [ ] **Step 5: 빌드 확인**

```bash
./gradlew compileDebugKotlin
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 6: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/widget/common/WidgetRefresher.kt app/src/main/java/com/pulmm/shiftcalendar/widget/common/WidgetAlarmScheduler.kt app/src/main/java/com/pulmm/shiftcalendar/widget/common/MidnightRefreshReceiver.kt app/src/main/java/com/pulmm/shiftcalendar/widget/common/BootCompletedReceiver.kt app/src/main/java/com/pulmm/shiftcalendar/ShiftCalendarApp.kt app/src/main/AndroidManifest.xml
git commit -m "위젯 자정 자동 갱신 스케줄러 추가"
```

---

## Task 15: 하루위젯 (1x1)

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/day/DayWidget.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/day/DayWidgetReceiver.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/day/DayWidgetConfigActivity.kt`
- Create: `app/src/main/res/xml/day_widget_info.xml`
- Modify: `app/src/main/AndroidManifest.xml`

실제 배치 최소 크기는 요청하신 "1x1"이 담을 정보(날짜/요일/근무이름/메모/음력, 최대 4줄)에 비해 너무 작아(안드로이드 1칸 ≈ 40dp) 글자가 겹치므로, 기본 크기를 2x2(약 110x110dp)로 잡고 최소 40dp까지 줄일 수 있게(`resizeMode`) 만든다. 이름은 "하루위젯"으로 그대로 유지한다.

- [ ] **Step 1: DayWidget 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.day

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pulmm.shiftcalendar.ShiftCalendarApp
import com.pulmm.shiftcalendar.logic.LunarConverter
import com.pulmm.shiftcalendar.widget.common.MemoEditActivity
import com.pulmm.shiftcalendar.widget.common.WidgetRoot
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

class DayWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as ShiftCalendarApp).repository
        val style = getAppWidgetState(context, PreferencesGlanceStateDefinition, id).toWidgetStyle()

        val today = LocalDate.now()
        val dayInfo = repository.getDayInfoOnce(today.toEpochDay())
        val shiftType = repository.getShiftTypesOnce().find { it.id == dayInfo.shiftTypeId }
        val lunar = if (style.showLunar) LunarConverter.toLunar(today) else null

        provideContent {
            WidgetRoot(style) {
                Column(
                    modifier = GlanceModifier.fillMaxSize().clickable(
                        actionStartActivity<MemoEditActivity>(
                            actionParametersOf(MemoEditActivity.EPOCH_DAY_KEY to today.toEpochDay())
                        )
                    )
                ) {
                    Text(
                        "${today.monthValue}월 ${today.dayOfMonth}일 ${today.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.KOREAN)}",
                        style = TextStyle(fontSize = (11 * style.dateFontScale).sp, color = ColorProvider(Color(0xFFCCCCCC)))
                    )
                    Text(
                        shiftType?.name ?: "근무 없음",
                        style = TextStyle(
                            fontSize = (22 * style.shiftFontScale).sp,
                            color = ColorProvider(shiftType?.colorArgb?.let { Color(it) } ?: Color.Gray)
                        )
                    )
                    if (!dayInfo.memoText.isNullOrBlank()) {
                        Text(
                            dayInfo.memoText,
                            style = TextStyle(fontSize = (10 * style.memoFontScale).sp, color = ColorProvider(Color(0xFFAAAAAA)))
                        )
                    }
                    if (lunar != null) {
                        Text(lunar.toShortDisplay(), style = TextStyle(fontSize = 8.sp, color = ColorProvider(Color(0xFF888888))))
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Receiver 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.day

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class DayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DayWidget()
}
```

- [ ] **Step 3: 구성 액티비티 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.day

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.pulmm.shiftcalendar.widget.common.DEFAULT_WIDGET_STYLE
import com.pulmm.shiftcalendar.widget.common.WidgetConfigContent
import com.pulmm.shiftcalendar.widget.common.applyStyle
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import kotlinx.coroutines.launch

class DayWidgetConfigActivity : ComponentActivity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val glanceId: GlanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)

        setContent {
            var initialStyle by remember { mutableStateOf(DEFAULT_WIDGET_STYLE) }
            androidx.compose.runtime.LaunchedEffect(appWidgetId) {
                initialStyle = getAppWidgetState(this@DayWidgetConfigActivity, PreferencesGlanceStateDefinition, glanceId).toWidgetStyle()
            }

            WidgetConfigContent(initialStyle = initialStyle, showLunarOption = true) { style ->
                lifecycleScope.launch {
                    updateAppWidgetState(this@DayWidgetConfigActivity, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                        prefs.toMutablePreferences().applyStyle(style).toPreferences()
                    }
                    DayWidget().update(this@DayWidgetConfigActivity, glanceId)
                    val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    setResult(Activity.RESULT_OK, resultValue)
                    finish()
                }
            }
        }
    }
}
```

- [ ] **Step 4: 위젯 정보 XML 작성**

`app/src/main/res/xml/day_widget_info.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="110dp"
    android:minHeight="110dp"
    android:targetCellWidth="2"
    android:targetCellHeight="2"
    android:minResizeWidth="40dp"
    android:minResizeHeight="40dp"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:previewImage="@mipmap/ic_launcher"
    android:configure="com.pulmm.shiftcalendar.widget.day.DayWidgetConfigActivity"
    android:updatePeriodMillis="0" />
```

- [ ] **Step 5: 매니페스트에 리시버/액티비티/메모편집 액티비티 등록**

`AndroidManifest.xml`의 `<application>` 안, `</application>` 바로 앞에 추가:
```xml
<receiver
    android:name=".widget.day.DayWidgetReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/day_widget_info" />
</receiver>

<activity
    android:name=".widget.day.DayWidgetConfigActivity"
    android:exported="false"
    android:theme="@style/Theme.ShiftCalendar.Dialog">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_CONFIGURE" />
    </intent-filter>
</activity>

<activity
    android:name=".widget.common.MemoEditActivity"
    android:exported="false"
    android:theme="@style/Theme.ShiftCalendar.Dialog" />
```

- [ ] **Step 6: 빌드 확인**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`. APK를 설치해 홈화면 길게 누르기 → 위젯 → 교대캘린더 → 하루위젯을 추가해보고 설정 화면이 뜨는지, 완료 후 오늘 근무가 표시되는지 눈으로 확인한다.

- [ ] **Step 7: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/widget/day app/src/main/res/xml/day_widget_info.xml app/src/main/AndroidManifest.xml
git commit -m "하루위젯(1x1) 추가"
```

---

## Task 16: 일주일위젯 (4x1)

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/week/WeekWidget.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/week/WeekWidgetReceiver.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/week/WeekWidgetConfigActivity.kt`
- Create: `app/src/main/res/xml/week_widget_info.xml`
- Modify: `app/src/main/AndroidManifest.xml`

일주일위젯도 하루 칸마다 요일/날짜/근무이름/메모까지 담아야 해서(최대 4줄), 실제 최소 크기는 요청하신 "4x1"(약 250x40dp)보다 살짝 큰 4x2(약 250x110dp)로 기본 배치하고, 40dp까지는 줄일 수 있게 한다.

- [ ] **Step 1: WeekWidget 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.week

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.pulmm.shiftcalendar.ShiftCalendarApp
import com.pulmm.shiftcalendar.logic.LunarConverter
import com.pulmm.shiftcalendar.widget.common.MemoEditActivity
import com.pulmm.shiftcalendar.widget.common.WidgetRoot
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.TextStyle as JavaTextStyle
import java.util.Locale

class WeekWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as ShiftCalendarApp).repository
        val style = getAppWidgetState(context, PreferencesGlanceStateDefinition, id).toWidgetStyle()

        val today = LocalDate.now()
        val weekStart = if (style.weekStartMonday) {
            today.minusDays(((today.dayOfWeek.value + 6) % 7).toLong())
        } else {
            today.minusDays((today.dayOfWeek.value % 7).toLong())
        }
        val weekDates = (0..6).map { weekStart.plusDays(it.toLong()) }
        val dayInfos = repository.observeDayInfoRange(weekStart.toEpochDay(), weekStart.plusDays(6).toEpochDay()).first()
        val shiftTypes = repository.getShiftTypesOnce()
        val dayInfoByEpochDay = dayInfos.associateBy { it.epochDay }
        val shiftTypeById = shiftTypes.associateBy { it.id }
        val lunarByEpochDay = if (style.showLunar) {
            weekDates.associate { d -> d.toEpochDay() to LunarConverter.toLunar(d) }
        } else emptyMap()

        provideContent {
            WidgetRoot(style) {
                Row(modifier = GlanceModifier.fillMaxSize()) {
                    weekDates.forEach { date ->
                        val dayInfo = dayInfoByEpochDay[date.toEpochDay()]
                        val shiftType = dayInfo?.shiftTypeId?.let { shiftTypeById[it] }
                        val lunar = lunarByEpochDay[date.toEpochDay()]
                        Column(
                            modifier = GlanceModifier
                                .defaultWeight()
                                .padding(2.dp)
                                .clickable(actionStartActivity<MemoEditActivity>(
                                    actionParametersOf(MemoEditActivity.EPOCH_DAY_KEY to date.toEpochDay())
                                ))
                        ) {
                            Text(
                                date.dayOfWeek.getDisplayName(JavaTextStyle.SHORT, Locale.KOREAN),
                                style = TextStyle(fontSize = (9 * style.dateFontScale).sp, color = ColorProvider(Color(0xFFAAAAAA)))
                            )
                            Text(
                                "${date.dayOfMonth}",
                                style = TextStyle(fontSize = (13 * style.dateFontScale).sp, color = ColorProvider(Color(0xFFEEEEEE)))
                            )
                            if (shiftType != null) {
                                Text(
                                    shiftType.name,
                                    style = TextStyle(fontSize = (9 * style.shiftFontScale).sp, color = ColorProvider(Color(shiftType.colorArgb)))
                                )
                                Box(modifier = GlanceModifier.fillMaxWidth().height(2.dp).background(ColorProvider(Color(shiftType.colorArgb))))
                            }
                            val memo = dayInfo?.memoText
                            if (!memo.isNullOrBlank()) {
                                Text(
                                    memo.take(2),
                                    style = TextStyle(fontSize = (8 * style.memoFontScale).sp, color = ColorProvider(Color(0xFFF1C40F)))
                                )
                            }
                            if (lunar != null) {
                                Text(lunar.toShortDisplay(), style = TextStyle(fontSize = 7.sp, color = ColorProvider(Color(0xFF888888))))
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **Step 2: Receiver 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.week

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class WeekWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeekWidget()
}
```

- [ ] **Step 3: 구성 액티비티 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.week

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.pulmm.shiftcalendar.widget.common.DEFAULT_WIDGET_STYLE
import com.pulmm.shiftcalendar.widget.common.WidgetConfigContent
import com.pulmm.shiftcalendar.widget.common.applyStyle
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import kotlinx.coroutines.launch

class WeekWidgetConfigActivity : ComponentActivity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val glanceId: GlanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)

        setContent {
            var initialStyle by remember { mutableStateOf(DEFAULT_WIDGET_STYLE) }
            LaunchedEffect(appWidgetId) {
                initialStyle = getAppWidgetState(this@WeekWidgetConfigActivity, PreferencesGlanceStateDefinition, glanceId).toWidgetStyle()
            }

            WidgetConfigContent(initialStyle = initialStyle, showLunarOption = true) { style ->
                lifecycleScope.launch {
                    updateAppWidgetState(this@WeekWidgetConfigActivity, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                        prefs.toMutablePreferences().applyStyle(style).toPreferences()
                    }
                    WeekWidget().update(this@WeekWidgetConfigActivity, glanceId)
                    val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    setResult(Activity.RESULT_OK, resultValue)
                    finish()
                }
            }
        }
    }
}
```

- [ ] **Step 4: 위젯 정보 XML 작성**

`app/src/main/res/xml/week_widget_info.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="250dp"
    android:minHeight="110dp"
    android:targetCellWidth="4"
    android:targetCellHeight="2"
    android:minResizeWidth="180dp"
    android:minResizeHeight="40dp"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:previewImage="@mipmap/ic_launcher"
    android:configure="com.pulmm.shiftcalendar.widget.week.WeekWidgetConfigActivity"
    android:updatePeriodMillis="0" />
```

- [ ] **Step 5: 매니페스트 등록**

`AndroidManifest.xml`의 `</application>` 바로 앞에 추가:
```xml
<receiver
    android:name=".widget.week.WeekWidgetReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/week_widget_info" />
</receiver>

<activity
    android:name=".widget.week.WeekWidgetConfigActivity"
    android:exported="false"
    android:theme="@style/Theme.ShiftCalendar.Dialog">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_CONFIGURE" />
    </intent-filter>
</activity>
```

- [ ] **Step 6: 빌드 확인**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`. 설치 후 일주일위젯을 추가해 이번 주 7일이 올바르게 나오는지 확인한다.

- [ ] **Step 7: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/widget/week app/src/main/res/xml/week_widget_info.xml app/src/main/AndroidManifest.xml
git commit -m "일주일위젯(4x1) 추가"
```

---

## Task 17: 작은한달위젯 (3x2)

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/monthsmall/SmallMonthWidget.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/monthsmall/SmallMonthWidgetReceiver.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/monthsmall/SmallMonthWidgetConfigActivity.kt`
- Create: `app/src/main/res/xml/small_month_widget_info.xml`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: SmallMonthWidget 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.monthsmall

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.pulmm.shiftcalendar.ShiftCalendarApp
import com.pulmm.shiftcalendar.widget.common.MonthGridView
import com.pulmm.shiftcalendar.widget.common.WeekdayHeaderRow
import com.pulmm.shiftcalendar.widget.common.WidgetRoot
import com.pulmm.shiftcalendar.widget.common.buildMonthGrid
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import kotlinx.coroutines.flow.first
import java.time.YearMonth

class SmallMonthWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as ShiftCalendarApp).repository
        val style = getAppWidgetState(context, PreferencesGlanceStateDefinition, id).toWidgetStyle()

        val month = YearMonth.now()
        val start = month.atDay(1).toEpochDay()
        val end = month.atEndOfMonth().toEpochDay()
        val dayInfos = repository.observeDayInfoRange(start, end).first()
        val shiftTypes = repository.getShiftTypesOnce()
        val dayInfoByEpochDay = dayInfos.associateBy { it.epochDay }
        val shiftTypeById = shiftTypes.associateBy { it.id }
        val weeks = buildMonthGrid(month, style.weekStartMonday)

        provideContent {
            WidgetRoot(style) {
                Column(modifier = androidx.glance.GlanceModifier.fillMaxSize()) {
                    WeekdayHeaderRow(style.weekStartMonday, style.dateFontScale)
                    MonthGridView(weeks, dayInfoByEpochDay, shiftTypeById, style, showShiftName = false)
                }
            }
        }
    }
}
```

- [ ] **Step 2: Receiver 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.monthsmall

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class SmallMonthWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallMonthWidget()
}
```

- [ ] **Step 3: 구성 액티비티 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.monthsmall

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.pulmm.shiftcalendar.widget.common.DEFAULT_WIDGET_STYLE
import com.pulmm.shiftcalendar.widget.common.WidgetConfigContent
import com.pulmm.shiftcalendar.widget.common.applyStyle
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import kotlinx.coroutines.launch

class SmallMonthWidgetConfigActivity : ComponentActivity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val glanceId: GlanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)

        setContent {
            var initialStyle by remember { mutableStateOf(DEFAULT_WIDGET_STYLE) }
            LaunchedEffect(appWidgetId) {
                initialStyle = getAppWidgetState(this@SmallMonthWidgetConfigActivity, PreferencesGlanceStateDefinition, glanceId).toWidgetStyle()
            }

            WidgetConfigContent(initialStyle = initialStyle, showLunarOption = false) { style ->
                lifecycleScope.launch {
                    updateAppWidgetState(this@SmallMonthWidgetConfigActivity, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                        prefs.toMutablePreferences().applyStyle(style).toPreferences()
                    }
                    SmallMonthWidget().update(this@SmallMonthWidgetConfigActivity, glanceId)
                    val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    setResult(Activity.RESULT_OK, resultValue)
                    finish()
                }
            }
        }
    }
}
```

- [ ] **Step 4: 위젯 정보 XML 작성**

`app/src/main/res/xml/small_month_widget_info.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="180dp"
    android:minHeight="110dp"
    android:targetCellWidth="3"
    android:targetCellHeight="2"
    android:minResizeWidth="110dp"
    android:minResizeHeight="110dp"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:previewImage="@mipmap/ic_launcher"
    android:configure="com.pulmm.shiftcalendar.widget.monthsmall.SmallMonthWidgetConfigActivity"
    android:updatePeriodMillis="0" />
```

- [ ] **Step 5: 매니페스트 등록**

`AndroidManifest.xml`의 `</application>` 바로 앞에 추가:
```xml
<receiver
    android:name=".widget.monthsmall.SmallMonthWidgetReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/small_month_widget_info" />
</receiver>

<activity
    android:name=".widget.monthsmall.SmallMonthWidgetConfigActivity"
    android:exported="false"
    android:theme="@style/Theme.ShiftCalendar.Dialog">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_CONFIGURE" />
    </intent-filter>
</activity>
```

- [ ] **Step 6: 빌드 확인**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 7: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/widget/monthsmall app/src/main/res/xml/small_month_widget_info.xml app/src/main/AndroidManifest.xml
git commit -m "작은한달위젯(3x2) 추가"
```

---

## Task 18: 한달위젯 (4x4) + 이전/다음 달 이동

**Files:**
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/month/MonthWidget.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/month/MonthOffsetAction.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/month/MonthWidgetReceiver.kt`
- Create: `app/src/main/java/com/pulmm/shiftcalendar/widget/month/MonthWidgetConfigActivity.kt`
- Create: `app/src/main/res/xml/month_widget_info.xml`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: 이전/다음 달 이동 액션 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.month

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.pulmm.shiftcalendar.widget.common.WidgetSettingsKeys

class MonthOffsetAction : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val delta = parameters[DELTA_KEY] ?: 0
        updateAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId) { prefs ->
            val current = prefs[WidgetSettingsKeys.MONTH_OFFSET] ?: 0
            prefs.toMutablePreferences().apply { this[WidgetSettingsKeys.MONTH_OFFSET] = current + delta }.toPreferences()
        }
        MonthWidget().update(context, glanceId)
    }

    companion object {
        val DELTA_KEY = ActionParameters.Key<Int>("delta")
    }
}
```

- [ ] **Step 2: MonthWidget 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.month

import android.content.Context
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.pulmm.shiftcalendar.ShiftCalendarApp
import com.pulmm.shiftcalendar.widget.common.MonthGridView
import com.pulmm.shiftcalendar.widget.common.WeekdayHeaderRow
import com.pulmm.shiftcalendar.widget.common.WidgetRoot
import com.pulmm.shiftcalendar.widget.common.WidgetSettingsKeys
import com.pulmm.shiftcalendar.widget.common.buildMonthGrid
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import kotlinx.coroutines.flow.first
import java.time.YearMonth

class MonthWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = (context.applicationContext as ShiftCalendarApp).repository
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
        val style = prefs.toWidgetStyle()
        val monthOffset = prefs[WidgetSettingsKeys.MONTH_OFFSET] ?: 0
        val month = YearMonth.now().plusMonths(monthOffset.toLong())

        val start = month.atDay(1).toEpochDay()
        val end = month.atEndOfMonth().toEpochDay()
        val dayInfos = repository.observeDayInfoRange(start, end).first()
        val shiftTypes = repository.getShiftTypesOnce()
        val dayInfoByEpochDay = dayInfos.associateBy { it.epochDay }
        val shiftTypeById = shiftTypes.associateBy { it.id }
        val weeks = buildMonthGrid(month, style.weekStartMonday)

        provideContent {
            WidgetRoot(style) {
                Column(modifier = GlanceModifier.fillMaxSize()) {
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        Text(
                            "◀",
                            modifier = GlanceModifier.clickable(
                                actionRunCallback<MonthOffsetAction>(actionParametersOf(MonthOffsetAction.DELTA_KEY to -1))
                            )
                        )
                        Text(
                            "${month.year}년 ${month.monthValue}월",
                            modifier = GlanceModifier.defaultWeight(),
                            style = TextStyle(fontSize = (12 * style.dateFontScale).sp)
                        )
                        Text(
                            "▶",
                            modifier = GlanceModifier.clickable(
                                actionRunCallback<MonthOffsetAction>(actionParametersOf(MonthOffsetAction.DELTA_KEY to 1))
                            )
                        )
                    }
                    WeekdayHeaderRow(style.weekStartMonday, style.dateFontScale)
                    MonthGridView(weeks, dayInfoByEpochDay, shiftTypeById, style, showShiftName = true)
                }
            }
        }
    }
}
```

- [ ] **Step 3: Receiver 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.month

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

class MonthWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MonthWidget()
}
```

- [ ] **Step 4: 구성 액티비티 작성**

```kotlin
package com.pulmm.shiftcalendar.widget.month

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import com.pulmm.shiftcalendar.widget.common.DEFAULT_WIDGET_STYLE
import com.pulmm.shiftcalendar.widget.common.WidgetConfigContent
import com.pulmm.shiftcalendar.widget.common.applyStyle
import com.pulmm.shiftcalendar.widget.common.toWidgetStyle
import kotlinx.coroutines.launch

class MonthWidgetConfigActivity : ComponentActivity() {
    private var appWidgetId: Int = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        val glanceId: GlanceId = GlanceAppWidgetManager(this).getGlanceIdBy(appWidgetId)

        setContent {
            var initialStyle by remember { mutableStateOf(DEFAULT_WIDGET_STYLE) }
            LaunchedEffect(appWidgetId) {
                initialStyle = getAppWidgetState(this@MonthWidgetConfigActivity, PreferencesGlanceStateDefinition, glanceId).toWidgetStyle()
            }

            WidgetConfigContent(initialStyle = initialStyle, showLunarOption = false) { style ->
                lifecycleScope.launch {
                    updateAppWidgetState(this@MonthWidgetConfigActivity, PreferencesGlanceStateDefinition, glanceId) { prefs ->
                        prefs.toMutablePreferences().applyStyle(style).toPreferences()
                    }
                    MonthWidget().update(this@MonthWidgetConfigActivity, glanceId)
                    val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    setResult(Activity.RESULT_OK, resultValue)
                    finish()
                }
            }
        }
    }
}
```

- [ ] **Step 5: 위젯 정보 XML 작성**

`app/src/main/res/xml/month_widget_info.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="250dp"
    android:minHeight="250dp"
    android:targetCellWidth="4"
    android:targetCellHeight="4"
    android:minResizeWidth="180dp"
    android:minResizeHeight="180dp"
    android:resizeMode="horizontal|vertical"
    android:widgetCategory="home_screen"
    android:previewImage="@mipmap/ic_launcher"
    android:configure="com.pulmm.shiftcalendar.widget.month.MonthWidgetConfigActivity"
    android:updatePeriodMillis="0" />
```

- [ ] **Step 6: 매니페스트 등록**

`AndroidManifest.xml`의 `</application>` 바로 앞에 추가:
```xml
<receiver
    android:name=".widget.month.MonthWidgetReceiver"
    android:exported="false">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/month_widget_info" />
</receiver>

<activity
    android:name=".widget.month.MonthWidgetConfigActivity"
    android:exported="false"
    android:theme="@style/Theme.ShiftCalendar.Dialog">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_CONFIGURE" />
    </intent-filter>
</activity>
```

- [ ] **Step 7: 빌드 확인**

```bash
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`. 설치 후 한달위젯을 추가해 ◀/▶ 탭으로 달이 바뀌는지 확인한다.

- [ ] **Step 8: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/widget/month app/src/main/res/xml/month_widget_info.xml app/src/main/AndroidManifest.xml
git commit -m "한달위젯(4x4)과 이전/다음 달 이동 추가"
```

---

## Task 19: 위젯 새로고침 연결 + 근무 종류 삭제 처리 확인 + 전체 빌드

**Files:**
- Modify: `app/src/main/java/com/pulmm/shiftcalendar/widget/common/WidgetRefresher.kt`
- Modify: `app/src/main/java/com/pulmm/shiftcalendar/ShiftCalendarApp.kt`
- Modify: `app/src/main/java/com/pulmm/shiftcalendar/ui/calendar/CalendarScreen.kt`

이제 4개 위젯이 모두 존재하므로, Task 7에서 비워뒀던 데이터 변경 → 위젯 새로고침 연결을 완성한다.

- [ ] **Step 1: WidgetRefresher 완성**

```kotlin
package com.pulmm.shiftcalendar.widget.common

import android.content.Context
import androidx.glance.appwidget.updateAll
import com.pulmm.shiftcalendar.widget.day.DayWidget
import com.pulmm.shiftcalendar.widget.month.MonthWidget
import com.pulmm.shiftcalendar.widget.monthsmall.SmallMonthWidget
import com.pulmm.shiftcalendar.widget.week.WeekWidget

object WidgetRefresher {
    suspend fun refreshAll(context: Context) {
        DayWidget().updateAll(context)
        WeekWidget().updateAll(context)
        SmallMonthWidget().updateAll(context)
        MonthWidget().updateAll(context)
    }
}
```

- [ ] **Step 2: ShiftCalendarApp이 repository 생성 시 이 콜백을 전달하도록 수정**

`ShiftCalendarApp.kt`:
```kotlin
package com.pulmm.shiftcalendar

import android.app.Application
import com.pulmm.shiftcalendar.data.AppDatabase
import com.pulmm.shiftcalendar.data.DataChangeListener
import com.pulmm.shiftcalendar.data.ShiftRepository
import com.pulmm.shiftcalendar.widget.common.WidgetAlarmScheduler
import com.pulmm.shiftcalendar.widget.common.WidgetRefresher

class ShiftCalendarApp : Application() {
    val repository: ShiftRepository by lazy {
        ShiftRepository(
            db = AppDatabase.getInstance(this),
            onDataChanged = DataChangeListener { WidgetRefresher.refreshAll(this) }
        )
    }

    override fun onCreate() {
        super.onCreate()
        WidgetAlarmScheduler.scheduleNextMidnight(this)
    }
}
```

- [ ] **Step 3: 근무 종류 삭제 시 달력 화면에서 "삭제됨" 회색 표시 확인**

`data/dao/ShiftPatternItem.kt`의 외래키가 `ShiftType`에 `onDelete = ForeignKey.CASCADE`로 걸려 있어, 근무 종류를 삭제하면 그 근무가 들어간 패턴의 해당 자리까지 함께 지워진다. 이는 "과거 데이터 보존" 요구(스펙 8번)에 어긋나므로 정책을 바꾼다.

`app/src/main/java/com/pulmm/shiftcalendar/data/entity/ShiftPatternItem.kt` 수정 (`onDelete` 정책 변경):
```kotlin
package com.pulmm.shiftcalendar.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shift_pattern_items",
    foreignKeys = [
        ForeignKey(entity = ShiftPattern::class, parentColumns = ["id"], childColumns = ["patternId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("patternId"), Index("shiftTypeId")]
)
data class ShiftPatternItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val patternId: Long,
    val shiftTypeId: Long,
    val orderIndex: Int
)
```
(`ShiftType`에 대한 외래키 제약을 제거해, 근무 종류가 삭제돼도 패턴 항목의 `shiftTypeId` 값은 그대로 남는다. `AppDatabase`의 `version`은 아직 배포 전이라 1을 유지해도 된다)

이미 `PatternRow`(Task 9)와 `MonthGrid`(Task 10)에서 `shiftTypeById[item.shiftTypeId]`가 `null`이면 `"삭제됨"`/회색으로 표시하도록 작성해뒀으므로 화면 쪽 추가 수정은 필요 없다. 위젯의 `MonthDayCell`/`WeekWidget`도 `shiftType`이 `null`이면 밑줄과 이름을 그리지 않아 자연스럽게 표시가 빠진다.

- [ ] **Step 4: 전체 빌드 + 테스트 확인**

```bash
cd "life/calendar_widget"
export JAVA_HOME="/c/dev-tools/jdk-17.0.20.1+1"
export ANDROID_HOME="/c/Android/sdk"
./gradlew test assembleDebug
```
Expected: `BUILD SUCCESSFUL`, 이전 태스크에서 작성한 단위 테스트(PatternCalculator/ShiftResolver/LunarConverter) 전부 통과

- [ ] **Step 5: 커밋**

```bash
git add app/src/main/java/com/pulmm/shiftcalendar/widget/common/WidgetRefresher.kt app/src/main/java/com/pulmm/shiftcalendar/ShiftCalendarApp.kt app/src/main/java/com/pulmm/shiftcalendar/data/entity/ShiftPatternItem.kt
git commit -m "데이터 변경 시 위젯 자동 새로고침 연결, 근무 종류 삭제 시 과거 데이터 보존"
```

---

## Task 20: 디버그 APK 빌드 및 전달

**Files:**
- (코드 변경 없음, 산출물만 생성)

- [ ] **Step 1: 최종 디버그 APK 빌드**

```bash
cd "life/calendar_widget"
export JAVA_HOME="/c/dev-tools/jdk-17.0.20.1+1"
export ANDROID_HOME="/c/Android/sdk"
./gradlew assembleDebug
```
Expected: `BUILD SUCCESSFUL`, `app/build/outputs/apk/debug/app-debug.apk` 생성됨

- [ ] **Step 2: 사용자에게 APK 전달**

`SendUserFile`로 `app/build/outputs/apk/debug/app-debug.apk`를 전달한다. 사용자는 이 파일을 안드로이드 휴대폰으로 옮겨 설치하면 된다(출처를 알 수 없는 앱 설치 허용 필요). 설치 후:
1. 앱을 열어 "근무종류" 탭에서 근무 종류(이름+색) 등록
2. "패턴" 탭에서 근무 순서와 시작일을 지정해 패턴 등록
3. "달력" 탭에서 자동으로 채워진 근무가 맞는지 확인
4. 홈 화면을 길게 눌러 위젯 4종을 각각 추가해보고 디자인 설정(투명도/색/글자크기/음력/주시작요일)이 잘 적용되는지 확인

- [ ] **Step 3: 최종 커밋 (필요 시)**

이 태스크는 빌드 산출물만 다루므로 별도 커밋은 없다. 지금까지의 모든 커밋이 이미 완료된 상태다.

---

## 부록: 스펙 커버리지 확인

| 설계 문서 항목 | 구현 태스크 |
|---|---|
| 근무 종류 자유 설정 | Task 8 |
| 근무 패턴 자동 반복 + 시작일 | Task 4, 9 |
| 날짜별 오버라이드(수동 수정) | Task 5, 10 |
| 메모(앱+위젯 양쪽 입력) | Task 10, 12 |
| 음력 표시(일주일/하루 위젯만) | Task 6, 15, 16 |
| 위젯 인스턴스별 독립 설정 | Task 12, 13, 15~18 |
| 한달/작은한달/일주일/하루 위젯 4종 | Task 15~18 |
| 위젯 자정 자동 갱신 | Task 14 |
| 데이터 변경 시 즉시 위젯 갱신 | Task 19 |
| 패턴 미등록 시 안내 | Task 15~18 (`shiftType`이 null이면 "근무 없음"/빈 칸 표시) |
| 근무 종류 삭제 시 과거 데이터 보존 | Task 19 |
| 완전 오프라인 동작 | 전체 (외부 API 호출 없음, 음력 변환도 로컬 라이브러리) |
