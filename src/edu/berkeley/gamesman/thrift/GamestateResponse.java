package edu.berkeley.gamesman.thrift;
/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.BitSet;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.thrift.*;
import org.apache.thrift.meta_data.*;
import org.apache.thrift.protocol.*;

/**
 * An object that directly maps to the JSON string to be returned by the GamesmanWeb server
 */
public class GamestateResponse implements TBase, java.io.Serializable, Cloneable, Comparable<GamestateResponse> {
  private static final TStruct STRUCT_DESC = new TStruct("GamestateResponse");
  private static final TField BOARD_FIELD_DESC = new TField("board", TType.STRING, (short)1);
  private static final TField REMOTENESS_FIELD_DESC = new TField("remoteness", TType.I32, (short)2);
  private static final TField VALUE_FIELD_DESC = new TField("value", TType.STRING, (short)3);
  private static final TField MOVE_FIELD_DESC = new TField("move", TType.STRING, (short)4);
  private static final TField SCORE_FIELD_DESC = new TField("score", TType.I32, (short)5);

  /**
   * If getMoveValue() is called, we use the following 3 fields
   */
  public String board;
  public int remoteness;
  public String value;
  /**
   * If getNextMoveValues() is called, we use the following field
   */
  public String move;
  /**
   * For certain games, we may also need to set the following fields
   */
  public int score;
  public static final int BOARD = 1;
  public static final int REMOTENESS = 2;
  public static final int VALUE = 3;
  public static final int MOVE = 4;
  public static final int SCORE = 5;

  // isset id assignments
  private static final int __REMOTENESS_ISSET_ID = 0;
  private static final int __SCORE_ISSET_ID = 1;
  private BitSet __isset_bit_vector = new BitSet(2);

  public static final Map<Integer, FieldMetaData> metaDataMap = Collections.unmodifiableMap(new HashMap<Integer, FieldMetaData>() {{
    put(BOARD, new FieldMetaData("board", TFieldRequirementType.OPTIONAL, 
        new FieldValueMetaData(TType.STRING)));
    put(REMOTENESS, new FieldMetaData("remoteness", TFieldRequirementType.OPTIONAL, 
        new FieldValueMetaData(TType.I32)));
    put(VALUE, new FieldMetaData("value", TFieldRequirementType.OPTIONAL, 
        new FieldValueMetaData(TType.STRING)));
    put(MOVE, new FieldMetaData("move", TFieldRequirementType.OPTIONAL, 
        new FieldValueMetaData(TType.STRING)));
    put(SCORE, new FieldMetaData("score", TFieldRequirementType.OPTIONAL, 
        new FieldValueMetaData(TType.I32)));
  }});

  static {
    FieldMetaData.addStructMetaDataMap(GamestateResponse.class, metaDataMap);
  }

  public GamestateResponse() {
  }

  /**
   * Performs a deep copy on <i>other</i>.
   */
  public GamestateResponse(GamestateResponse other) {
    __isset_bit_vector.clear();
    __isset_bit_vector.or(other.__isset_bit_vector);
    if (other.isSetBoard()) {
      this.board = other.board;
    }
    this.remoteness = other.remoteness;
    if (other.isSetValue()) {
      this.value = other.value;
    }
    if (other.isSetMove()) {
      this.move = other.move;
    }
    this.score = other.score;
  }

  public GamestateResponse deepCopy() {
    return new GamestateResponse(this);
  }

  @Deprecated
  public GamestateResponse clone() {
    return new GamestateResponse(this);
  }

  /**
   * If getMoveValue() is called, we use the following 3 fields
   */
  public String getBoard() {
    return this.board;
  }

  /**
   * If getMoveValue() is called, we use the following 3 fields
   */
  public GamestateResponse setBoard(String board) {
    this.board = board;
    return this;
  }

  public void unsetBoard() {
    this.board = null;
  }

  // Returns true if field board is set (has been asigned a value) and false otherwise
  public boolean isSetBoard() {
    return this.board != null;
  }

  public void setBoardIsSet(boolean value) {
    if (!value) {
      this.board = null;
    }
  }

  public int getRemoteness() {
    return this.remoteness;
  }

  public GamestateResponse setRemoteness(int remoteness) {
    this.remoteness = remoteness;
    setRemotenessIsSet(true);
    return this;
  }

  public void unsetRemoteness() {
    __isset_bit_vector.clear(__REMOTENESS_ISSET_ID);
  }

  // Returns true if field remoteness is set (has been asigned a value) and false otherwise
  public boolean isSetRemoteness() {
    return __isset_bit_vector.get(__REMOTENESS_ISSET_ID);
  }

  public void setRemotenessIsSet(boolean value) {
    __isset_bit_vector.set(__REMOTENESS_ISSET_ID, value);
  }

  public String getValue() {
    return this.value;
  }

  public GamestateResponse setValue(String value) {
    this.value = value;
    return this;
  }

  public void unsetValue() {
    this.value = null;
  }

  // Returns true if field value is set (has been asigned a value) and false otherwise
  public boolean isSetValue() {
    return this.value != null;
  }

  public void setValueIsSet(boolean value) {
    if (!value) {
      this.value = null;
    }
  }

  /**
   * If getNextMoveValues() is called, we use the following field
   */
  public String getMove() {
    return this.move;
  }

  /**
   * If getNextMoveValues() is called, we use the following field
   */
  public GamestateResponse setMove(String move) {
    this.move = move;
    return this;
  }

  public void unsetMove() {
    this.move = null;
  }

  // Returns true if field move is set (has been asigned a value) and false otherwise
  public boolean isSetMove() {
    return this.move != null;
  }

  public void setMoveIsSet(boolean value) {
    if (!value) {
      this.move = null;
    }
  }

  /**
   * For certain games, we may also need to set the following fields
   */
  public int getScore() {
    return this.score;
  }

  /**
   * For certain games, we may also need to set the following fields
   */
  public GamestateResponse setScore(int score) {
    this.score = score;
    setScoreIsSet(true);
    return this;
  }

  public void unsetScore() {
    __isset_bit_vector.clear(__SCORE_ISSET_ID);
  }

  // Returns true if field score is set (has been asigned a value) and false otherwise
  public boolean isSetScore() {
    return __isset_bit_vector.get(__SCORE_ISSET_ID);
  }

  public void setScoreIsSet(boolean value) {
    __isset_bit_vector.set(__SCORE_ISSET_ID, value);
  }

  public void setFieldValue(int fieldID, Object value) {
    switch (fieldID) {
    case BOARD:
      if (value == null) {
        unsetBoard();
      } else {
        setBoard((String)value);
      }
      break;

    case REMOTENESS:
      if (value == null) {
        unsetRemoteness();
      } else {
        setRemoteness((Integer)value);
      }
      break;

    case VALUE:
      if (value == null) {
        unsetValue();
      } else {
        setValue((String)value);
      }
      break;

    case MOVE:
      if (value == null) {
        unsetMove();
      } else {
        setMove((String)value);
      }
      break;

    case SCORE:
      if (value == null) {
        unsetScore();
      } else {
        setScore((Integer)value);
      }
      break;

    default:
      throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
    }
  }

  public Object getFieldValue(int fieldID) {
    switch (fieldID) {
    case BOARD:
      return getBoard();

    case REMOTENESS:
      return new Integer(getRemoteness());

    case VALUE:
      return getValue();

    case MOVE:
      return getMove();

    case SCORE:
      return new Integer(getScore());

    default:
      throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
    }
  }

  // Returns true if field corresponding to fieldID is set (has been asigned a value) and false otherwise
  public boolean isSet(int fieldID) {
    switch (fieldID) {
    case BOARD:
      return isSetBoard();
    case REMOTENESS:
      return isSetRemoteness();
    case VALUE:
      return isSetValue();
    case MOVE:
      return isSetMove();
    case SCORE:
      return isSetScore();
    default:
      throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
    }
  }

  @Override
  public boolean equals(Object that) {
    if (that == null)
      return false;
    if (that instanceof GamestateResponse)
      return this.equals((GamestateResponse)that);
    return false;
  }

  public boolean equals(GamestateResponse that) {
    if (that == null)
      return false;

    boolean this_present_board = true && this.isSetBoard();
    boolean that_present_board = true && that.isSetBoard();
    if (this_present_board || that_present_board) {
      if (!(this_present_board && that_present_board))
        return false;
      if (!this.board.equals(that.board))
        return false;
    }

    boolean this_present_remoteness = true && this.isSetRemoteness();
    boolean that_present_remoteness = true && that.isSetRemoteness();
    if (this_present_remoteness || that_present_remoteness) {
      if (!(this_present_remoteness && that_present_remoteness))
        return false;
      if (this.remoteness != that.remoteness)
        return false;
    }

    boolean this_present_value = true && this.isSetValue();
    boolean that_present_value = true && that.isSetValue();
    if (this_present_value || that_present_value) {
      if (!(this_present_value && that_present_value))
        return false;
      if (!this.value.equals(that.value))
        return false;
    }

    boolean this_present_move = true && this.isSetMove();
    boolean that_present_move = true && that.isSetMove();
    if (this_present_move || that_present_move) {
      if (!(this_present_move && that_present_move))
        return false;
      if (!this.move.equals(that.move))
        return false;
    }

    boolean this_present_score = true && this.isSetScore();
    boolean that_present_score = true && that.isSetScore();
    if (this_present_score || that_present_score) {
      if (!(this_present_score && that_present_score))
        return false;
      if (this.score != that.score)
        return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    return 0;
  }

  public int compareTo(GamestateResponse other) {
    if (!getClass().equals(other.getClass())) {
      return getClass().getName().compareTo(other.getClass().getName());
    }

    int lastComparison = 0;
    GamestateResponse typedOther = (GamestateResponse)other;

    lastComparison = Boolean.valueOf(isSetBoard()).compareTo(isSetBoard());
    if (lastComparison != 0) {
      return lastComparison;
    }
    lastComparison = TBaseHelper.compareTo(board, typedOther.board);
    if (lastComparison != 0) {
      return lastComparison;
    }
    lastComparison = Boolean.valueOf(isSetRemoteness()).compareTo(isSetRemoteness());
    if (lastComparison != 0) {
      return lastComparison;
    }
    lastComparison = TBaseHelper.compareTo(remoteness, typedOther.remoteness);
    if (lastComparison != 0) {
      return lastComparison;
    }
    lastComparison = Boolean.valueOf(isSetValue()).compareTo(isSetValue());
    if (lastComparison != 0) {
      return lastComparison;
    }
    lastComparison = TBaseHelper.compareTo(value, typedOther.value);
    if (lastComparison != 0) {
      return lastComparison;
    }
    lastComparison = Boolean.valueOf(isSetMove()).compareTo(isSetMove());
    if (lastComparison != 0) {
      return lastComparison;
    }
    lastComparison = TBaseHelper.compareTo(move, typedOther.move);
    if (lastComparison != 0) {
      return lastComparison;
    }
    lastComparison = Boolean.valueOf(isSetScore()).compareTo(isSetScore());
    if (lastComparison != 0) {
      return lastComparison;
    }
    lastComparison = TBaseHelper.compareTo(score, typedOther.score);
    if (lastComparison != 0) {
      return lastComparison;
    }
    return 0;
  }

  public void read(TProtocol iprot) throws TException {
    TField field;
    iprot.readStructBegin();
    while (true)
    {
      field = iprot.readFieldBegin();
      if (field.type == TType.STOP) { 
        break;
      }
      switch (field.id)
      {
        case BOARD:
          if (field.type == TType.STRING) {
            this.board = iprot.readString();
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case REMOTENESS:
          if (field.type == TType.I32) {
            this.remoteness = iprot.readI32();
            setRemotenessIsSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case VALUE:
          if (field.type == TType.STRING) {
            this.value = iprot.readString();
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case MOVE:
          if (field.type == TType.STRING) {
            this.move = iprot.readString();
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        case SCORE:
          if (field.type == TType.I32) {
            this.score = iprot.readI32();
            setScoreIsSet(true);
          } else { 
            TProtocolUtil.skip(iprot, field.type);
          }
          break;
        default:
          TProtocolUtil.skip(iprot, field.type);
          break;
      }
      iprot.readFieldEnd();
    }
    iprot.readStructEnd();


    // check for required fields of primitive type, which can't be checked in the validate method
    validate();
  }

  public void write(TProtocol oprot) throws TException {
    validate();

    oprot.writeStructBegin(STRUCT_DESC);
    if (this.board != null) {
      if (isSetBoard()) {
        oprot.writeFieldBegin(BOARD_FIELD_DESC);
        oprot.writeString(this.board);
        oprot.writeFieldEnd();
      }
    }
    if (isSetRemoteness()) {
      oprot.writeFieldBegin(REMOTENESS_FIELD_DESC);
      oprot.writeI32(this.remoteness);
      oprot.writeFieldEnd();
    }
    if (this.value != null) {
      if (isSetValue()) {
        oprot.writeFieldBegin(VALUE_FIELD_DESC);
        oprot.writeString(this.value);
        oprot.writeFieldEnd();
      }
    }
    if (this.move != null) {
      if (isSetMove()) {
        oprot.writeFieldBegin(MOVE_FIELD_DESC);
        oprot.writeString(this.move);
        oprot.writeFieldEnd();
      }
    }
    if (isSetScore()) {
      oprot.writeFieldBegin(SCORE_FIELD_DESC);
      oprot.writeI32(this.score);
      oprot.writeFieldEnd();
    }
    oprot.writeFieldStop();
    oprot.writeStructEnd();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("GamestateResponse(");
    boolean first = true;

    if (isSetBoard()) {
      sb.append("board:");
      if (this.board == null) {
        sb.append("null");
      } else {
        sb.append(this.board);
      }
      first = false;
    }
    if (isSetRemoteness()) {
      if (!first) sb.append(", ");
      sb.append("remoteness:");
      sb.append(this.remoteness);
      first = false;
    }
    if (isSetValue()) {
      if (!first) sb.append(", ");
      sb.append("value:");
      if (this.value == null) {
        sb.append("null");
      } else {
        sb.append(this.value);
      }
      first = false;
    }
    if (isSetMove()) {
      if (!first) sb.append(", ");
      sb.append("move:");
      if (this.move == null) {
        sb.append("null");
      } else {
        sb.append(this.move);
      }
      first = false;
    }
    if (isSetScore()) {
      if (!first) sb.append(", ");
      sb.append("score:");
      sb.append(this.score);
      first = false;
    }
    sb.append(")");
    return sb.toString();
  }

  public void validate() throws TException {
    // check for required fields
    // check that fields of type enum have valid values
  }

}
