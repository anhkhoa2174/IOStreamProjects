package co.iostream.apps.code_pocket.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
//import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.AdSize
//import com.google.android.gms.ads.AdView
//import co.iostream.apps.file_locker_x.configs.AdConfig
//
//@Composable
//fun BannerAdView(_adUnitId: String = AdConfig.SampleAdUnit.BANNER.id) {
//    Column(
//        modifier = Modifier.fillMaxWidth()
//    ) {
////        Text(text = stringResource(id = R.string.advertisement), modifier = Modifier.padding(8.dp, 0.dp))
//        Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
//        Spacer(modifier = Modifier.height(4.dp))
//        Row(modifier = Modifier.height(AdSize.BANNER.height.dp)) {
//            AndroidView(modifier = Modifier.fillMaxWidth(), factory = { context ->
//                AdView(context).apply {
//                    setAdSize(AdSize.BANNER)
//                    adUnitId = _adUnitId
//                    loadAd(AdRequest.Builder().build())
//                }
//            })
//        }
//        Spacer(modifier = Modifier.height(4.dp))
//        Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)
//    }
//}