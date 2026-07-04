package com.rmws2002.noteapp.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
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
    Quote("天行健，君子以自强不息。", "《周易》"),
    Quote("地势坤，君子以厚德载物。", "《周易》"),
    Quote("穷则变，变则通，通则久。", "《周易》"),
    Quote("日日行，不怕千万里；常常做，不怕千万事。", "金缨"),
    Quote("有志者，事竟成，破釜沉舟，百二秦关终属楚。", "蒲松龄"),
    Quote("苦心人，天不负，卧薪尝胆，三千越甲可吞吴。", "蒲松龄"),
    Quote("一个人的价值，应该看他贡献什么，而不应当看他取得什么。", "爱因斯坦"),
    Quote("生活就像骑自行车，要想保持平衡就得不断前进。", "爱因斯坦"),
    Quote("想象力比知识更重要。", "爱因斯坦"),
    Quote("天才是百分之一的灵感加上百分之九十九的汗水。", "爱迪生"),
    Quote("Stay hungry, stay foolish.", "Steve Jobs"),
    Quote("The only way to do great work is to love what you do.", "Steve Jobs"),
    Quote("Simplicity is the ultimate sophistication.", "Leonardo da Vinci"),
    Quote("简单是复杂的最高境界。", "达·芬奇"),
    Quote("我们听过无数的道理，却仍旧过不好这一生。", "韩寒"),
    Quote("世界上只有一种真正的英雄主义，那就是认清生活真相后依然热爱生活。", "罗曼·罗兰"),
    Quote("人生最大的荣耀，不在于从不失败，而在于每次跌倒都能重新站起。", "曼德拉"),
    Quote("要么你主宰生活，要么你被生活主宰。", "吉姆·罗恩"),
    Quote("高效能人士的七个习惯：积极主动、以终为始、要事第一。", "史蒂芬·柯维"),
    Quote("未雨绸缪，事半功倍。", "谚语"),
    Quote("工欲善其事，必先利其器。", "孔子"),
    Quote("三人行，必有我师焉。择其善者而从之，其不善者而改之。", "孔子"),
    Quote("温故而知新，可以为师矣。", "孔子"),
    Quote("知之为知之，不知为不知，是知也。", "孔子"),
    Quote("吾生也有涯，而知也无涯。", "庄子"),
    Quote("博学之，审问之，慎思之，明辨之，笃行之。", "《中庸》"),
    Quote("玉不琢，不成器；人不学，不知道。", "《礼记》"),
    Quote("凡事预则立，不预则废。", "《礼记》"),
    Quote("合抱之木，生于毫末；九层之台，起于累土。", "老子"),
    Quote("大音希声，大象无形。", "老子"),
    Quote("上善若水，水善利万物而不争。", "老子"),
    Quote("岁月本长，而忙者自促。", "《菜根谭》"),
    Quote("宠辱不惊，看庭前花开花落；去留无意，望天上云卷云舒。", "《菜根谭》"),
    Quote("此心安处是吾乡。", "苏轼"),
    Quote("当你为错过太阳而哭泣的时候，你也要再错过群星了。", "泰戈尔"),
    Quote("世界上最宽阔的是海洋，比海洋更宽阔的是天空，比天空更宽阔的是人的心灵。", "雨果"),
    Quote("今天应做的事没有做，明天再早也是耽误了。", "裴斯泰洛齐"),
    Quote("做你自己，因为别人都有人做了。", "王尔德"),
    Quote("你热爱生命吗？那么别浪费时间，因为时间是组成生命的材料。", "富兰克林"),
    Quote("早起的鸟儿有虫吃。", "谚语"),
    Quote("积土成山，风雨兴焉；积水成渊，蛟龙生焉。", "荀子"),
    Quote("骐骥一跃，不能十步；驽马十驾，功在不舍。", "荀子"),
    Quote("锲而舍之，朽木不折；锲而不舍，金石可镂。", "荀子"),
)

/**
 * Picks a daily quote based on the day of the year.
 * Same quote all day, rotates every midnight.
 */
fun getDailyQuote(): Quote {
    val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
    return quotes[dayOfYear % quotes.size]
}

@Composable
fun DailyQuote(
    modifier: Modifier = Modifier
) {
    val quote = remember { getDailyQuote() }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            // Left accent bar (blockquote style)
            Spacer(
                modifier = Modifier
                    .width(3.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "“${quote.text}”",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontStyle = FontStyle.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "— ${quote.author}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
