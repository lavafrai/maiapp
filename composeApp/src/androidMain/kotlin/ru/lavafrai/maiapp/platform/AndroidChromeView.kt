package ru.lavafrai.maiapp.ru.lavafrai.maiapp.platform

import android.R
import android.app.PendingIntent
import android.content.*
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent


class AndroidChromeView {
    companion object {
        private val TOOLBAR_SHARE_ITEM_ID: Int = 1

        fun openTab(context: Context, url: String?) {
            val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()

            enableUrlBarHiding(builder)
            addShareMenuItem(builder)

            val customTabsIntent: CustomTabsIntent = builder.build()
            customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            customTabsIntent.launchUrl(context, Uri.parse(url))
        }

        private fun enableUrlBarHiding(builder: CustomTabsIntent.Builder) {
            builder.enableUrlBarHiding()
        }

        private fun setShareActionButton(context: Context, builder: CustomTabsIntent.Builder, url: String) {
            val icon = BitmapFactory.decodeResource(context.resources, R.drawable.ic_menu_share)
            val label = "Share via"

            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.putExtra(Intent.EXTRA_TEXT, url)
            shareIntent.setType("text/plain")

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                shareIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            builder.setActionButton(icon, label, pendingIntent)
        }

        private fun addShareMenuItem(builder: CustomTabsIntent.Builder) {
            builder.addDefaultShareMenuItem()
        }

        class CopyBroadcastReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val url = intent.dataString

                val clipboardManager: ClipboardManager =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val data = ClipData.newPlainText("Link", url)
                clipboardManager.setPrimaryClip(data)

                Toast.makeText(context, "Copied $url", Toast.LENGTH_SHORT).show()
            }
        }
    }
}