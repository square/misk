package misk.feature.launchdarkly

import wisp.feature.DynamicConfig
import wisp.feature.Feature
import wisp.feature.FeatureFlags
import java.util.concurrent.Executor
import javax.inject.Singleton

@Singleton
class LaunchDarklyDynamicConfig(featureFlags: FeatureFlags) : DynamicConfig {

  private val delegate: wisp.launchdarkly.LaunchDarklyDynamicConfig =
    wisp.launchdarkly.LaunchDarklyDynamicConfig(featureFlags)

  override fun getBoolean(feature: Feature) =
    delegate.getBoolean(feature)

  override fun getInt(feature: Feature) =
    delegate.getInt(feature)

  override fun getString(feature: Feature) =
    delegate.getString(feature)

  override fun <T : Enum<T>> getEnum(feature: Feature, clazz: Class<T>) =
    delegate.getEnum(feature, clazz)

  override fun <T> getJson(feature: Feature, clazz: Class<T>) =
    delegate.getJson(feature, clazz)

  override fun trackBoolean(feature: Feature, executor: Executor, tracker: (Boolean) -> Unit) =
    delegate.trackBoolean(feature, executor, tracker)

  override fun trackInt(feature: Feature, executor: Executor, tracker: (Int) -> Unit) =
    delegate.trackInt(feature, executor, tracker)

  override fun trackString(feature: Feature, executor: Executor, tracker: (String) -> Unit) =
    delegate.trackString(feature, executor, tracker)

  override fun <T : Enum<T>> trackEnum(
    feature: Feature,
    clazz: Class<T>,
    executor: Executor,
    tracker: (T) -> Unit
  ) = delegate.trackEnum(feature, clazz, executor, tracker)

  override fun <T> trackJson(
    feature: Feature,
    clazz: Class<T>,
    executor: Executor,
    tracker: (T) -> Unit
  ) = delegate.trackJson(feature, clazz, executor, tracker)
}
