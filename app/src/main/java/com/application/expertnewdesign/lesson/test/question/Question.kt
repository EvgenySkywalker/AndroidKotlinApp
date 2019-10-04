package com.application.expertnewdesign.lesson.test.question
import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import java.io.File

const val DEFAULT_TEXT_SIZE = 16f

//Сущность любого вопроса
@Serializable
open class QuestionBase(
    val questionText: String,
    val questionID: Int,
    val maxGrade: Int
){
    override fun toString(): String {
        return "QuestionText: $questionText\nID: $questionID\n"
    }

    @Serializer(forClass = QuestionBase::class) companion object : KSerializer<QuestionBase> {
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("QuestionBase") {
            init {
                addElement("questionText")
                addElement("questionID")
                addElement("maxGrade")
            }
        }

        override fun serialize(encoder: Encoder, obj: QuestionBase) {
            val compositeOutput = encoder.beginStructure(descriptor)
            compositeOutput.encodeStringElement(descriptor, 0, obj.questionText)
            compositeOutput.encodeIntElement(descriptor, 1, obj.questionID)
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): QuestionBase {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var questionText: String? = null
            var questionID: Int? = null
            var maxGrade: Int? = null
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> questionText = dec.decodeStringElement(descriptor, i)
                    1 -> questionID = dec.decodeIntElement(descriptor, i)
                    2 -> maxGrade = dec.decodeIntElement(descriptor, i)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)
            return QuestionBase(
                questionText ?: throw MissingFieldException("questionText"),
                questionID ?: throw MissingFieldException("questionID"),
                maxGrade ?: 1
            )
        }
    }
}

//Вопрос с единственным правильным вариантом ответа
@Serializable
class SingleAnswerQuestionBase(
    questionText: String = "",
    questionID: Int = 0,
    maxGrade: Int = 1,
    val correctAnswer : String = "",
    val incorrectAnswers : List<String> = emptyList())
    : QuestionBase(questionText, questionID, maxGrade){

    override fun toString(): String {
        return "${super.toString()}CorrectAnswer: $correctAnswer\nIncorrectAnswers: $incorrectAnswers"
    }

    @Serializer(forClass = SingleAnswerQuestionBase::class) companion object : KSerializer<SingleAnswerQuestionBase> {
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("SingleAnswerQuestion") {
            init {
                addElement("questionBasic")
                addElement("correctAnswer")
                addElement("incorrectAnswers")
            }
        }

        override fun serialize(encoder: Encoder, obj: SingleAnswerQuestionBase) {
            val compositeOutput = encoder.beginStructure(descriptor)
            compositeOutput.encodeStringElement(descriptor, 0, obj.questionText)
            compositeOutput.encodeStringElement(descriptor, 1, obj.correctAnswer)
            compositeOutput.encodeSerializableElement( descriptor, 2, String.serializer().list, obj.incorrectAnswers)
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): SingleAnswerQuestionBase {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var questionBase: QuestionBase? = null
            var correctAnswer: String? = null
            var incorrectAnswers: List<String>? = null
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> questionBase = dec.decodeSerializableElement(descriptor, i, QuestionBase.serializer())
                    1 -> correctAnswer = dec.decodeStringElement(descriptor, i)
                    2 -> incorrectAnswers = dec.decodeSerializableElement(descriptor, i, String.serializer().list)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)
            return SingleAnswerQuestionBase(
                questionBase?.questionText ?: throw MissingFieldException("questionBase"),
                questionBase.questionID,
                questionBase.maxGrade,
                correctAnswer ?: throw MissingFieldException("correctAnswer"),
                incorrectAnswers ?: throw MissingFieldException("incorrectAnswers")
            )
        }
    }
}

//Вопрос с множеством правильных вариантов ответа
@Serializable
class MultipleAnswerQuestionBase(
    questionText: String = "",
    questionID: Int = 0,
    maxGrade: Int = 1,
    var correctAnswers : List<String> = emptyList(),
    var incorrectAnswers : List<String> = emptyList())
    : QuestionBase(questionText, questionID, maxGrade){

    override fun toString(): String {
        return "${super.toString()}CorrectAnswers: $correctAnswers\nIncorrectAnswers: $incorrectAnswers"
    }

    @Serializer(forClass = MultipleAnswerQuestionBase::class) companion object : KSerializer<MultipleAnswerQuestionBase>{
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("MultipleAnswerQuestion") {
            init {
                addElement("questionBasic")
                addElement("correctAnswers")
                addElement("incorrectAnswers")
            }
        }

        override fun serialize(encoder: Encoder, obj: MultipleAnswerQuestionBase) {
            val compositeOutput = encoder.beginStructure(descriptor)
            compositeOutput.encodeStringElement(descriptor, 0, obj.questionText)
            compositeOutput.encodeSerializableElement(descriptor, 1, String.serializer().list, obj.correctAnswers)
            compositeOutput.encodeSerializableElement(descriptor, 1, String.serializer().list, obj.incorrectAnswers)
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): MultipleAnswerQuestionBase {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var questionBase : QuestionBase? = null
            var correctAnswers : List<String>? = null
            var incorrectAnswers : List<String>? = null

            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> questionBase = dec.decodeSerializableElement(descriptor, i, QuestionBase.serializer())
                    1 -> correctAnswers = dec.decodeSerializableElement(descriptor, i, String.serializer().list)
                    2 -> incorrectAnswers = dec.decodeSerializableElement(descriptor, i, String.serializer().list)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)

            return MultipleAnswerQuestionBase(
                questionBase?.questionText ?: throw MissingFieldException("questionBase"),
                questionBase.questionID,
                questionBase.maxGrade,
                correctAnswers ?: throw MissingFieldException("correctAnswers"),
                incorrectAnswers ?: throw MissingFieldException("incorrectAnswers")

            )
        }
    }
}


@Serializable
data class MatchPair(val first : String, val second : String)


@Serializable
class MatchQuestionBase(
    questionText: String = "",
    questionID: Int = 0,
    maxGrade: Int = 1,
    val pairs : List<Pair<String, String>> = emptyList(),
    val incorrectAnswers : List<String> = emptyList())
    : QuestionBase(questionText, questionID, maxGrade){

    override fun toString(): String {
        return "${super.toString()}Pairs: $pairs\nIncorrectAnswers: $incorrectAnswers\n"
    }

    @Serializer(forClass = MatchQuestionBase::class) companion object : KSerializer<MatchQuestionBase> {
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("MatchQuestionBase") {
            init {
                addElement("questionBasic")
                addElement("pairs")
                addElement("incorrectAnswers")
            }
        }

        override fun serialize(encoder: Encoder, obj: MatchQuestionBase) {
            val compositeOutput = encoder.beginStructure(descriptor)
            //placeholder
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): MatchQuestionBase {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var questionBase : QuestionBase? = null
            var pairs : List<MatchPair>? = null
            var incorrectAnswers : List<String>? = null

            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> questionBase = dec.decodeSerializableElement(descriptor, i, QuestionBase.serializer())
                    1 -> pairs = dec.decodeSerializableElement(descriptor, i, MatchPair.serializer().list)
                    2 -> incorrectAnswers = dec.decodeSerializableElement(descriptor, i, String.serializer().list)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)

            if(pairs == null)
                throw MissingFieldException("pairs")

            val finalPairs = emptyList<Pair<String, String>>().toMutableList()
            pairs.forEach {
                finalPairs.add(it.first to it.second)
            }

            return MatchQuestionBase(
                questionBase?.questionText ?: throw MissingFieldException("questionBase"),
                questionBase.questionID,
                questionBase.maxGrade,
                finalPairs,
                incorrectAnswers ?: throw MissingFieldException("incorrectAnswers")
            )
        }
    }

}

@Serializable
class OneWordQuestionBase(
    questionText: String = "",
    questionID: Int = 0,
    maxGrade: Int = 1,
    val correctAnswer : String
)
    : QuestionBase(questionText, questionID, maxGrade){

    override fun toString(): String {
        return "${super.toString()}CorrectAnswer: $correctAnswer\n"
    }

    @Serializer(forClass = OneWordQuestionBase::class) companion object : KSerializer<OneWordQuestionBase> {
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("OneWordQuestionBase") {
            init {
                addElement("questionBasic")
                addElement("correctAnswer")
            }
        }

        override fun serialize(encoder: Encoder, obj: OneWordQuestionBase) {
            val compositeOutput = encoder.beginStructure(descriptor)
            compositeOutput.encodeStringElement(descriptor, 0, obj.questionText)
            compositeOutput.encodeStringElement(descriptor, 1, obj.correctAnswer)
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): OneWordQuestionBase {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var questionBase: QuestionBase? = null
            var correctAnswer: String? = null
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> questionBase = dec.decodeSerializableElement(descriptor, i, QuestionBase.serializer())
                    1 -> correctAnswer = dec.decodeStringElement(descriptor, i)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)
            return OneWordQuestionBase(
                questionBase?.questionText ?: throw MissingFieldException("questionBase"),
                questionBase.questionID,
                questionBase.maxGrade,
                correctAnswer ?: throw MissingFieldException("correctAnswer")
            )
        }
    }
}

@Serializable
class ChronologicalQuestionBase(
    questionText: String = "",
    questionID: Int = 0,
    maxGrade: Int = 1,
    val correctOrder : List<String>
)
    :QuestionBase(questionText, questionID, maxGrade){

    override fun toString(): String {
        return "${super.toString()}CorrectOrder: $correctOrder\n"
    }

    @Serializer(forClass = ChronologicalQuestionBase::class) companion object : KSerializer<ChronologicalQuestionBase> {
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("ChronologicalQuestionBase") {
            init {
                addElement("questionBasic")
                addElement("correctOrder")
            }
        }

        override fun serialize(encoder: Encoder, obj: ChronologicalQuestionBase) {
            val compositeOutput = encoder.beginStructure(descriptor)
            //placeholder
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): ChronologicalQuestionBase {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var questionBase: QuestionBase? = null
            var correctOrder: List<String>? = null
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> questionBase = dec.decodeSerializableElement(descriptor, i, QuestionBase.serializer())
                    1 -> correctOrder = dec.decodeSerializableElement(descriptor, i, String.serializer().list)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)
            return ChronologicalQuestionBase(
                questionBase?.questionText ?: throw MissingFieldException("questionBase"),
                questionBase.questionID,
                questionBase.maxGrade,
                correctOrder ?: throw MissingFieldException("correctOrder")
            )
        }
    }
}

@Serializable
class MultipleAnswerQuestionEGE_Base(
    questionText: String = "",
    questionID: Int = 0,
    maxGrade: Int = 1,
    val correctAnswer: String
) : QuestionBase(questionText, questionID, maxGrade){

    @Serializer(forClass = MultipleAnswerQuestionEGE_Base::class) companion object : KSerializer<MultipleAnswerQuestionEGE_Base> {
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("MultipleAnswerQuestionEGE_Base") {
            init {
                addElement("questionBasic")
                addElement("correctAnswer")
            }
        }

        override fun serialize(encoder: Encoder, obj: MultipleAnswerQuestionEGE_Base) {
            val compositeOutput = encoder.beginStructure(descriptor)
            //placeholder
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): MultipleAnswerQuestionEGE_Base {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var questionBase: QuestionBase? = null
            var correctAnswer: String? = null
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> questionBase = dec.decodeSerializableElement(descriptor, i, QuestionBase.serializer())
                    1 -> correctAnswer = dec.decodeStringElement(descriptor, i)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)
            return MultipleAnswerQuestionEGE_Base(
                questionBase?.questionText ?: throw MissingFieldException("questionBase"),
                questionBase.questionID,
                questionBase.maxGrade,
                correctAnswer ?: throw MissingFieldException("correctOrder")
            )
        }
    }

}

@Serializable
class MatchQuestionEGE_Base(
    questionText: String = "",
    questionID: Int = 0,
    maxGrade: Int = 1,
    val correctAnswer: String
) : QuestionBase(questionText, questionID, maxGrade){

    @Serializer(forClass = MatchQuestionEGE_Base::class) companion object : KSerializer<MatchQuestionEGE_Base> {
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("MatchQuestionEGE_Base") {
            init {
                addElement("questionBasic")
                addElement("correctAnswer")
            }
        }

        override fun serialize(encoder: Encoder, obj: MatchQuestionEGE_Base) {
            val compositeOutput = encoder.beginStructure(descriptor)
            //placeholder
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): MatchQuestionEGE_Base {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var questionBase: QuestionBase? = null
            var correctAnswer: String? = null
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> questionBase = dec.decodeSerializableElement(descriptor, i, QuestionBase.serializer())
                    1 -> correctAnswer = dec.decodeStringElement(descriptor, i)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)
            return MatchQuestionEGE_Base(
                questionBase?.questionText ?: throw MissingFieldException("questionBase"),
                questionBase.questionID,
                questionBase.maxGrade,
                correctAnswer ?: throw MissingFieldException("correctOrder")
            )
        }
    }

}

@Serializable
class PairMatchQuestionEGE_Base(
    questionText: String = "",
    questionID: Int = 0,
    maxGrade: Int = 1,
    val correctAnswer: String
) : QuestionBase(questionText, questionID, maxGrade){

    @Serializer(forClass = PairMatchQuestionEGE_Base::class) companion object : KSerializer<PairMatchQuestionEGE_Base> {
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("MatchQuestionEGE_Base") {
            init {
                addElement("questionBasic")
                addElement("correctAnswer")
            }
        }

        override fun serialize(encoder: Encoder, obj: PairMatchQuestionEGE_Base) {
            val compositeOutput = encoder.beginStructure(descriptor)
            //placeholder
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): PairMatchQuestionEGE_Base {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var questionBase: QuestionBase? = null
            var correctAnswer: String? = null
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> questionBase = dec.decodeSerializableElement(descriptor, i, QuestionBase.serializer())
                    1 -> correctAnswer = dec.decodeStringElement(descriptor, i)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)
            return PairMatchQuestionEGE_Base(
                questionBase?.questionText ?: throw MissingFieldException("questionBase"),
                questionBase.questionID,
                questionBase.maxGrade,
                correctAnswer ?: throw MissingFieldException("correctOrder")
            )
        }
    }

}

@Serializable
class SequenceQuestionEGE_Base(
    questionText: String = "",
    questionID: Int = 0,
    maxGrade: Int = 1,
    val correctAnswer: String
) : QuestionBase(questionText, questionID, maxGrade){

    @Serializer(forClass = SequenceQuestionEGE_Base::class) companion object : KSerializer<SequenceQuestionEGE_Base> {
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("MatchQuestionEGE_Base") {
            init {
                addElement("questionBasic")
                addElement("correctAnswer")
            }
        }

        override fun serialize(encoder: Encoder, obj: SequenceQuestionEGE_Base) {
            val compositeOutput = encoder.beginStructure(descriptor)
            //placeholder
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): SequenceQuestionEGE_Base {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var questionBase: QuestionBase? = null
            var correctAnswer: String? = null
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> questionBase = dec.decodeSerializableElement(descriptor, i, QuestionBase.serializer())
                    1 -> correctAnswer = dec.decodeStringElement(descriptor, i)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)
            return SequenceQuestionEGE_Base(
                questionBase?.questionText ?: throw MissingFieldException("questionBase"),
                questionBase.questionID,
                questionBase.maxGrade,
                correctAnswer ?: throw MissingFieldException("correctOrder")
            )
        }
    }

}

@Serializable
class OneWordQuestionEGE_Base(
    questionText: String = "",
    questionID: Int = 0,
    maxGrade: Int = 1,
    val correctAnswer: String
) : QuestionBase(questionText, questionID, maxGrade){

    @Serializer(forClass = OneWordQuestionEGE_Base::class) companion object : KSerializer<OneWordQuestionEGE_Base> {
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("MatchQuestionEGE_Base") {
            init {
                addElement("questionBasic")
                addElement("correctAnswer")
            }
        }

        override fun serialize(encoder: Encoder, obj: OneWordQuestionEGE_Base) {
            val compositeOutput = encoder.beginStructure(descriptor)
            //placeholder
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): OneWordQuestionEGE_Base {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var questionBase: QuestionBase? = null
            var correctAnswer: String? = null
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> questionBase = dec.decodeSerializableElement(descriptor, i, QuestionBase.serializer())
                    1 -> correctAnswer = dec.decodeStringElement(descriptor, i)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)
            return OneWordQuestionEGE_Base(
                questionBase?.questionText ?: throw MissingFieldException("questionBase"),
                questionBase.questionID,
                questionBase.maxGrade,
                correctAnswer ?: throw MissingFieldException("correctOrder")
            )
        }
    }

}

@Serializable
class QuestionMetadata(val questionType : String = "", val questionBase: QuestionBase){
    @Serializer(forClass = QuestionMetadata::class) companion object : KSerializer<QuestionMetadata>{
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("BinaryPayload") {
            init {
                addElement("questionType")
                addElement("questionData")
            }
        }

        override fun serialize(encoder: Encoder, obj: QuestionMetadata) {
            val compositeOutput = encoder.beginStructure(descriptor)
            compositeOutput.encodeStringElement(descriptor, 0, obj.questionType)
            println(obj.questionType)
            when(obj.questionType){
                "SingleAnswerQuestion" ->
                    compositeOutput.encodeSerializableElement(descriptor, 1,
                        SingleAnswerQuestionBase.serializer(), obj.questionBase as SingleAnswerQuestionBase)
                "MultipleAnswerQuestion" ->
                    compositeOutput.encodeSerializableElement(descriptor, 1,
                        MultipleAnswerQuestionBase.serializer(), obj.questionBase as MultipleAnswerQuestionBase)
                "MatchQuestion" ->
                    compositeOutput.encodeSerializableElement(descriptor, 1,
                        MatchQuestionBase.serializer(), obj.questionBase as MatchQuestionBase)
                else -> throw SerializationException("Unknown questionType")
            }
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): QuestionMetadata {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var questionType : String? = null
            var questionBase : QuestionBase? = null

            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> questionType = dec.decodeStringElement(descriptor, i)
                    1 -> questionBase = dec.decodeSerializableElement(descriptor, i,
                        when(questionType){
                            "SingleAnswerQuestion" -> SingleAnswerQuestionBase.serializer()
                            "MultipleAnswerQuestion" -> MultipleAnswerQuestionBase.serializer()
                            "MatchQuestion" -> MatchQuestionBase.serializer()
                            "OneWordQuestion" -> OneWordQuestionBase.serializer()
                            "ChronologicalQuestion" -> ChronologicalQuestionBase.serializer()
                            "SequenceQuestionEGE"       -> SequenceQuestionEGE_Base.serializer()
                            "OneWordQuestionEGE"        -> OneWordQuestionEGE_Base.serializer()
                            "MatchQuestionEGE"          -> MatchQuestionEGE_Base.serializer()
                            "PairMatchQuestionEGE"      -> PairMatchQuestionEGE_Base.serializer()
                            "MultipleAnswerQuestionEGE" -> MultipleAnswerQuestionEGE_Base.serializer()
                            null -> throw SerializationException("questionType is yet unknown")
                            else -> throw SerializationException("Unknown questionType")
                        }
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)

            return QuestionMetadata(
                questionType ?: throw MissingFieldException("questionType"),
                questionBase ?: throw MissingFieldException("questionBase")
            )
        }
    }
}

/*
//Вопрос на сопоставление
class DragQuestionBase(
questionText: String = "",
questionID: Int,
var leftColumn: List<String> = emptyList(),
var rightColumn: List<String> = emptyList(), var otherOptions : List<String> = emptyList())
: QuestionBase(questionText, questionID){


}


override fun toXML(file : File, spacing : Int) {
      //Формируем отступ
      val tabs = XML.getSpacing(spacing)

      //Открывающий тэг
      file.appendText("$tabs<${XML.Fields.SingleAnswerQuestion.lesson} ${XML.Fields.questionText.lesson}=\"$questionText\">\n")

      //Правильный ответ
      file.appendText("$tabs\t${XML.Fields.correctAnswer.openTag}$correctAnswer${XML.Fields.correctAnswer.closeTag}\n")

      //Неправильные ответы
      for(option in incorrectAnswers)
          file.appendText("$tabs\t${XML.Fields.wrongAnswer.openTag}$option${XML.Fields.wrongAnswer.closeTag}\n")

      //Закрывающий тэг
      file.appendText("$tabs${XML.Fields.SingleAnswerQuestion.closeTag}\n")
  }

override fun toXML(file : File, spacing : Int) {
      //Формируем отступ
      val tabs = XML.getSpacing(spacing)

      //Открывающий тэг
      file.appendText("$tabs<${XML.Fields.DragQuestion.lesson} ${XML.Fields.questionText.lesson}=\"$questionText\">\n")

      //Правильные ответы
      for(i in (0 until leftColumn.size)){
          file.appendText("$tabs\t${XML.Fields.pair.openTag}\n")
          file.appendText("$tabs\t\t${XML.Fields.left.openTag}${leftColumn[i]}${XML.Fields.left.closeTag}\n")
          file.appendText("$tabs\t\t${XML.Fields.right.openTag}${rightColumn[i]}${XML.Fields.right.closeTag}\n")
          file.appendText("$tabs\t${XML.Fields.pair.closeTag}\n")
      }

      //Неправильные ответы
      for(option in otherOptions)
          file.appendText("$tabs\t${XML.Fields.wrongAnswer.openTag}$option${XML.Fields.wrongAnswer.closeTag}\n")

      //Закрывающий тэг
      file.appendText("$tabs${XML.Fields.DragQuestion.closeTag}\n")
  }

fun toXML(file : File, spacing : Int){}

override fun toXML(file : File, spacing : Int) {
      //Формируем отступ
      val tabs = XML.getSpacing(spacing)

      //Открывающий тэг
      file.appendText("$tabs<${XML.Fields.MultipleAnswerQuestion.lesson} ${XML.Fields.questionText.lesson}=\"$questionText\">\n")

      //Правильные ответы
      for(correctAnswer in correctAnswers)
          file.appendText("$tabs\t${XML.Fields.correctAnswer.openTag}$correctAnswer${XML.Fields.correctAnswer.closeTag}\n")

      //Неправильные ответы
      for(option in incorrectAnswers)
          file.appendText("$tabs\t${XML.Fields.wrongAnswer.openTag}$option${XML.Fields.wrongAnswer.closeTag}\n")

      //Закрывающий тэг
      file.appendText("$tabs${XML.Fields.MultipleAnswerQuestion.closeTag}\n")
  }
*/


//Объекты парсинга
/*
object XML{
    fun getSpacing(spacing: Int) : String{
        var tabs = ""
        for(i in (0 until spacing))
            tabs += "\t"
        return tabs
    }

    class Field(val lesson : String){
        val openTag = "<$lesson>"
        val closeTag = "</$lesson>"
    }

    object Fields{
        val correctAnswer = Field("correctAnswer")
        val wrongAnswer = Field("wrongAnswer")
        val left = Field("left")
        val right = Field("right")
        val pair = Field("pair")
        val SingleAnswerQuestion = Field("SingleAnswerQuestion")
        val MultipleAnswerQuestion = Field("MultipleAnswerQuestion")
        val DragQuestion = Field("DragQuestion")
        val questionText = Field("questionText")
        val QuestionPack = Field("QuestionPack")
    }


    class ParseException(parsedObject: String, parser : XmlPullParser)
        : Exception("Couldn't parse $parsedObject. Event: ${parser.eventType} Name: ${parser.lesson} Text: ${parser.text}")

    class QuestionParser(file : File) {

        private val parser: XmlPullParser = XmlPullParserFactory.newInstance().newPullParser()

        init {
            parser.setInput(file.bufferedReader())
            parser.next()
        }

        fun getQuestionPack(context: Context) : QuestionPack {

            val questions = emptyList<Question>().toMutableList()

            if(!(parser.eventType == XmlPullParser.START_TAG && parser.lesson == Fields.QuestionPack.lesson)){
                throw ParseException(Fields.QuestionPack.lesson, parser)
            } else {
                parser.next()
            }

            while (!(parser.eventType == XmlPullParser.END_TAG && parser.lesson == Fields.QuestionPack.lesson)) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        questions.add(
                            when (parser.lesson) {
                                Fields.SingleAnswerQuestion.lesson -> getSAQ(context)
                                Fields.MultipleAnswerQuestion.lesson -> getMAQ(context)
                                Fields.DragQuestion.lesson -> getDQ(context)
                                else -> throw ParseException(Fields.QuestionPack.lesson, parser)
                            })
                    }
                    XmlPullParser.TEXT -> {}
                    else -> throw ParseException(Fields.QuestionPack.lesson, parser)
                }
                parser.next()
            }
            parser.next()

            return QuestionPack(context, questions)
        }

        private fun getSAQ(context: Context) : SingleAnswerQuestion {

            var correctAnswer = ""
            val questionText : String
            val otherOptions : MutableList<String> = emptyList<String>().toMutableList()

            if(!(parser.eventType == XmlPullParser.START_TAG && parser.lesson == Fields.SingleAnswerQuestion.lesson)){
                throw ParseException(Fields.SingleAnswerQuestion.lesson, parser)
            } else {
                questionText = parser.getAttributeValue(null, Fields.questionText.lesson)
                parser.next()
            }

            while (!(parser.eventType == XmlPullParser.END_TAG && parser.lesson == Fields.SingleAnswerQuestion.lesson)) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.lesson) {
                            Fields.correctAnswer.lesson -> correctAnswer = parseCorrectAnswer()
                            Fields.wrongAnswer.lesson -> otherOptions.add(parseWrongAnswer())
                            else -> throw ParseException(Fields.SingleAnswerQuestion.lesson, parser)
                        }
                    }
                    XmlPullParser.TEXT -> {}
                    else -> throw ParseException(Fields.SingleAnswerQuestion.lesson, parser)
                }
                parser.next()
            }
            parser.next()

            return SingleAnswerQuestion(context, SingleAnswerQuestionBase(questionText, correctAnswer, otherOptions))
        }

        private fun parseGenericString(string : String) : String{

            val result: String

            if(!(parser.eventType == XmlPullParser.START_TAG && parser.lesson == string)) {
                throw ParseException(string, parser)
            }else{
                parser.next()

                if (parser.eventType != XmlPullParser.TEXT) {
                    throw ParseException(string, parser)
                } else {
                    result = parser.text
                    parser.next()
                }

                if(!(parser.eventType == XmlPullParser.END_TAG && parser.lesson == string)) {
                    throw ParseException(string, parser)
                }else{
                    parser.next()
                }
            }

            return result
        }

        private fun getMAQ(context: Context) : MultipleAnswerQuestion {

            val questionText : String
            val correctAnswers : MutableList<String> = emptyList<String>().toMutableList()
            val otherOptions : MutableList<String> = emptyList<String>().toMutableList()

            if(!(parser.eventType == XmlPullParser.START_TAG && parser.lesson == Fields.MultipleAnswerQuestion.lesson)){
                throw ParseException(Fields.MultipleAnswerQuestion.lesson, parser)
            } else {
                questionText = parser.getAttributeValue(null, Fields.questionText.lesson)
                parser.next()
            }

            while (!(parser.eventType == XmlPullParser.END_TAG && parser.lesson == Fields.MultipleAnswerQuestion.lesson)) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.lesson) {
                            Fields.correctAnswer.lesson -> correctAnswers.add(parseCorrectAnswer())
                            Fields.wrongAnswer.lesson -> otherOptions.add(parseWrongAnswer())
                            else -> throw ParseException(Fields.MultipleAnswerQuestion.lesson, parser)
                        }
                    }
                    XmlPullParser.TEXT -> {}
                    else -> throw ParseException(Fields.MultipleAnswerQuestion.lesson, parser)
                }
                parser.next()
            }
            parser.next()

            return MultipleAnswerQuestion(context, MultipleAnswerQuestionBase(questionText, correctAnswers, otherOptions))
        }

        private fun getDQ(context: Context) : DragQuestion {

            val questionText : String
            val leftColumn : MutableList<String> = emptyList<String>().toMutableList()
            val rightColumn : MutableList<String> = emptyList<String>().toMutableList()
            val otherOptions : MutableList<String> = emptyList<String>().toMutableList()

            if(!(parser.eventType == XmlPullParser.START_TAG && parser.lesson == Fields.DragQuestion.lesson)){
                throw ParseException(Fields.DragQuestion.lesson, parser)
            } else {
                questionText = parser.getAttributeValue(null, Fields.questionText.lesson)
                parser.next()
            }

            while (!(parser.eventType == XmlPullParser.END_TAG && parser.lesson == Fields.DragQuestion.lesson)) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.lesson) {
                            Fields.pair.lesson -> {
                                val pair = parsePair()
                                leftColumn.add(pair.first)
                                rightColumn.add(pair.second)
                            }
                            Fields.wrongAnswer.lesson -> otherOptions.add(parseWrongAnswer())
                            else -> throw ParseException(Fields.DragQuestion.lesson, parser)
                        }
                    }
                    XmlPullParser.TEXT -> {}
                    else -> throw ParseException(Fields.DragQuestion.lesson, parser)
                }
                parser.next()
            }
            parser.next()

            return DragQuestion(context, DragQuestionBase(questionText, leftColumn, rightColumn, otherOptions))
        }

        private fun parseCorrectAnswer() : String{
            return parseGenericString(Fields.correctAnswer.lesson)
        }

        private fun parseWrongAnswer() : String{
            return parseGenericString(Fields.wrongAnswer.lesson)
        }

        private fun parseLeft() : String {
            return parseGenericString(Fields.left.lesson)
        }

        private fun parseRight() : String {
            return parseGenericString(Fields.right.lesson)
        }

        private fun parsePair() : Pair<String, String>{

            var left = ""
            var right = ""

            if(!(parser.eventType == XmlPullParser.START_TAG && parser.lesson == Fields.pair.lesson)) {
                throw ParseException(Fields.pair.lesson, parser)
            }else{
                parser.next()
                while (!(parser.eventType == XmlPullParser.END_TAG && parser.lesson == Fields.pair.lesson)) {
                    when (parser.eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.lesson) {
                                Fields.left.lesson -> left = parseLeft()
                                Fields.right.lesson -> right = parseRight()
                                else -> throw ParseException(Fields.pair.lesson, parser)
                            }
                        }
                        XmlPullParser.TEXT -> {}
                        else -> throw ParseException(Fields.pair.lesson, parser)
                    }
                    parser.next()
                }
                parser.next()
            }

            return Pair(left, right)
        }
    }


}
*/