package com.rmws2002.noteapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import java.util.Calendar

data class Quote(val text: String, val author: String)

private val quotes = listOf(
    Quote("不积跬步，无以至千里；不积小流，无以成江海。", "荀子"),
    Quote("学而不思则罔，思而不学则殆。", "孔子"),
    Quote("知之者不如好之者，好之者不如乐之者。", "孔子"),
    Quote("千里之行，始于足下。", "老子"),
    Quote("天下难事，必作于易；天下大事，必作于细。", "老子"),
    Quote("业精于勤，荒于嬉；行成于思，毁于随。", "韩愈"),
    Quote("书山有路勤为径，学海无涯苦作舟。", "韩愈"),
    Quote("少壮不努力，老大徒伤悲。", "汉乐府"),
    Quote("盛年不重来，一日难再晨。及时当勉励，岁月不待人。", "陶渊明"),
    Quote("非淡泊无以明志，非宁静无以致远。", "诸葛亮"),
    Quote("路漫漫其修远兮，吾将上下而求索。", "屈原"),
    Quote("宝剑锋从磨砺出，梅花香自苦寒来。", "佚名"),
    Quote("莫等闲，白了少年头，空悲切。", "岳飞"),
    Quote("纸上得来终觉浅，绝知此事要躬行。", "陆游"),
    Quote("山重水复疑无路，柳暗花明又一村。", "陆游"),
    Quote("不畏浮云遮望眼，自缘身在最高层。", "王安石"),
    Quote("问渠那得清如许？为有源头活水来。", "朱熹"),
    Quote("长风破浪会有时，直挂云帆济沧海。", "李白"),
    Quote("天生我材必有用，千金散尽还复来。", "李白"),
    Quote("会当凌绝顶，一览众山小。", "杜甫"),
    Quote("海内存知己，天涯若比邻。", "王勃"),
    Quote("落霞与孤鹜齐飞，秋水共长天一色。", "王勃"),
    Quote("大漠孤烟直，长河落日圆。", "王维"),
    Quote("行到水穷处，坐看云起时。", "王维"),
    Quote("采菊东篱下，悠然见南山。", "陶渊明"),
    Quote("竹杖芒鞋轻胜马，谁怕？一蓑烟雨任平生。", "苏轼"),
    Quote("但愿人长久，千里共婵娟。", "苏轼"),
    Quote("人生如逆旅，我亦是行人。", "苏轼"),
    Quote("粗缯大布裹生涯，腹有诗书气自华。", "苏轼"),
    Quote("此心安处是吾乡。", "苏轼"),
    Quote("天行健，君子以自强不息。", "《周易》"),
    Quote("地势坤，君子以厚德载物。", "《周易》"),
    Quote("穷则变，变则通，通则久。", "《周易》"),
    Quote("日日行，不怕千万里；常常做，不怕千万事。", "金缨"),
    Quote("有志者，事竟成，破釜沉舟，百二秦关终属楚。", "蒲松龄"),
    Quote("苦心人，天不负，卧薪尝胆，三千越甲可吞吴。", "蒲松龄"),
    Quote("吾生也有涯，而知也无涯。", "庄子"),
    Quote("博学之，审问之，慎思之，明辨之，笃行之。", "《中庸》"),
    Quote("玉不琢，不成器；人不学，不知道。", "《礼记》"),
    Quote("凡事预则立，不预则废。", "《礼记》"),
    Quote("合抱之木，生于毫末；九层之台，起于累土。", "老子"),
    Quote("大音希声，大象无形。", "老子"),
    Quote("上善若水，水善利万物而不争。", "老子"),
    Quote("锲而舍之，朽木不折；锲而不舍，金石可镂。", "荀子"),
    Quote("骐骥一跃，不能十步；驽马十驾，功在不舍。", "荀子"),
    Quote("积土成山，风雨兴焉；积水成渊，蛟龙生焉。", "荀子"),
    Quote("三人行，必有我师焉。择其善者而从之，其不善者而改之。", "孔子"),
    Quote("温故而知新，可以为师矣。", "孔子"),
    Quote("知之为知之，不知为不知，是知也。", "孔子"),
    Quote("工欲善其事，必先利其器。", "孔子"),
    Quote("己所不欲，勿施于人。", "孔子"),
    Quote("岁月本长，而忙者自促。", "《菜根谭》"),
    Quote("宠辱不惊，看庭前花开花落；去留无意，望天上云卷云舒。", "《菜根谭》"),
    Quote("世事洞明皆学问，人情练达即文章。", "曹雪芹"),
    Quote("满纸荒唐言，一把辛酸泪。都云作者痴，谁解其中味？", "曹雪芹"),
    Quote("山不在高，有仙则名。水不在深，有龙则灵。", "刘禹锡"),
    Quote("沉舟侧畔千帆过，病树前头万木春。", "刘禹锡"),
    Quote("先天下之忧而忧，后天下之乐而乐。", "范仲淹"),
    Quote("醉翁之意不在酒，在乎山水之间也。", "欧阳修"),
    Quote("出淤泥而不染，濯清涟而不妖。", "周敦颐"),
    Quote("一言既出，驷马难追。", "《论语》"),
    Quote("学而时习之，不亦说乎？", "孔子"),
    Quote("道生一，一生二，二生三，三生万物。", "老子"),
    Quote("桃李不言，下自成蹊。", "司马迁"),
    Quote("燕雀安知鸿鹄之志哉？", "陈涉"),
    Quote("大器晚成，大音希声。", "老子"),
    Quote("海阔凭鱼跃，天高任鸟飞。", "佚名"),
    Quote("一寸光阴一寸金，寸金难买寸光阴。", "谚语"),
    Quote("近朱者赤，近墨者黑。", "傅玄"),
    Quote("读万卷书，行万里路。", "董其昌"),
    Quote("十年磨一剑，霜刃未曾试。", "贾岛"),
    Quote("春蚕到死丝方尽，蜡炬成灰泪始干。", "李商隐"),
)

fun getDailyQuote(): Quote {
    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    return quotes[dayOfYear % quotes.size]
}

@Composable
fun DailyQuote(modifier: Modifier = Modifier) {
    val quote = remember { getDailyQuote() }

    Column(modifier = modifier.fillMaxWidth()) {
        // Quote text with subtle left accent
        androidx.compose.foundation.layout.Row(modifier = Modifier.fillMaxWidth()) {
            Spacer(
                modifier = Modifier
                    .width(3.dp)
                    .height(36.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quote.text,
                    style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Normal),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
