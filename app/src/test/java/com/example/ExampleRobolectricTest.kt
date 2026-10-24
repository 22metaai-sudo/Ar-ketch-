package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.DrawingRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("AR Trace", appName)
  }

  @Test
  fun `verify curated templates catalog exceeds 500 items`() {
    val templates = DrawingRepository.getCuratedTemplates()
    assertTrue(templates.size >= 500)
    val categories = DrawingRepository.getCategories()
    assertTrue(categories.contains("Anime & Manga"))
    assertTrue(categories.contains("Hyper-Réaliste & Réaliste"))
  }

  @Test
  fun `verify tutorials and inspiration feed`() {
    val tutorials = DrawingRepository.getTutorials()
    assertTrue(tutorials.isNotEmpty())
    val feed = DrawingRepository.getInspirationFeed()
    assertTrue(feed.isNotEmpty())
  }
}
