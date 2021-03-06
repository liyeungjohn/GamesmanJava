package edu.berkeley.gamesman.thrift;
/**
 * Autogenerated by Thrift
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 */
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TBaseHelper;
import org.apache.thrift.TException;
import org.apache.thrift.TFieldRequirementType;
import org.apache.thrift.TProcessor;
import org.apache.thrift.meta_data.FieldMetaData;
import org.apache.thrift.meta_data.FieldValueMetaData;
import org.apache.thrift.meta_data.StructMetaData;
import org.apache.thrift.protocol.TField;
import org.apache.thrift.protocol.TMessage;
import org.apache.thrift.protocol.TMessageType;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TProtocolUtil;
import org.apache.thrift.protocol.TStruct;
import org.apache.thrift.protocol.TType;

public class GamestateRequestHandler {

  public interface Iface {

    public GetNextMoveResponse getNextMoveValues(String gameName, String configuration) throws TException;

    public GetMoveResponse getMoveValue(String gameName, String configuration) throws TException;

  }

  public static class Client implements Iface {
    public Client(TProtocol prot)
    {
      this(prot, prot);
    }

    public Client(TProtocol iprot, TProtocol oprot)
    {
      iprot_ = iprot;
      oprot_ = oprot;
    }

    protected TProtocol iprot_;
    protected TProtocol oprot_;

    protected int seqid_;

    public TProtocol getInputProtocol()
    {
      return this.iprot_;
    }

    public TProtocol getOutputProtocol()
    {
      return this.oprot_;
    }

    public GetNextMoveResponse getNextMoveValues(String gameName, String configuration) throws TException
    {
      send_getNextMoveValues(gameName, configuration);
      return recv_getNextMoveValues();
    }

    public void send_getNextMoveValues(String gameName, String configuration) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("getNextMoveValues", TMessageType.CALL, seqid_));
      getNextMoveValues_args args = new getNextMoveValues_args();
      args.gameName = gameName;
      args.configuration = configuration;
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }

    public GetNextMoveResponse recv_getNextMoveValues() throws TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      getNextMoveValues_result result = new getNextMoveValues_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "getNextMoveValues failed: unknown result");
    }

    public GetMoveResponse getMoveValue(String gameName, String configuration) throws TException
    {
      send_getMoveValue(gameName, configuration);
      return recv_getMoveValue();
    }

    public void send_getMoveValue(String gameName, String configuration) throws TException
    {
      oprot_.writeMessageBegin(new TMessage("getMoveValue", TMessageType.CALL, seqid_));
      getMoveValue_args args = new getMoveValue_args();
      args.gameName = gameName;
      args.configuration = configuration;
      args.write(oprot_);
      oprot_.writeMessageEnd();
      oprot_.getTransport().flush();
    }

    public GetMoveResponse recv_getMoveValue() throws TException
    {
      TMessage msg = iprot_.readMessageBegin();
      if (msg.type == TMessageType.EXCEPTION) {
        TApplicationException x = TApplicationException.read(iprot_);
        iprot_.readMessageEnd();
        throw x;
      }
      getMoveValue_result result = new getMoveValue_result();
      result.read(iprot_);
      iprot_.readMessageEnd();
      if (result.isSetSuccess()) {
        return result.success;
      }
      throw new TApplicationException(TApplicationException.MISSING_RESULT, "getMoveValue failed: unknown result");
    }

  }
  public static class Processor implements TProcessor {
    public Processor(Iface iface)
    {
      iface_ = iface;
      processMap_.put("getNextMoveValues", new getNextMoveValues());
      processMap_.put("getMoveValue", new getMoveValue());
    }

    protected static interface ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException;
    }

    private Iface iface_;
    protected final HashMap<String,ProcessFunction> processMap_ = new HashMap<String,ProcessFunction>();

    public boolean process(TProtocol iprot, TProtocol oprot) throws TException
    {
      TMessage msg = iprot.readMessageBegin();
      ProcessFunction fn = processMap_.get(msg.name);
      if (fn == null) {
        TProtocolUtil.skip(iprot, TType.STRUCT);
        iprot.readMessageEnd();
        TApplicationException x = new TApplicationException(TApplicationException.UNKNOWN_METHOD, "Invalid method name: '"+msg.name+"'");
        oprot.writeMessageBegin(new TMessage(msg.name, TMessageType.EXCEPTION, msg.seqid));
        x.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
        return true;
      }
      fn.process(msg.seqid, iprot, oprot);
      return true;
    }

    private class getNextMoveValues implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        getNextMoveValues_args args = new getNextMoveValues_args();
        args.read(iprot);
        iprot.readMessageEnd();
        getNextMoveValues_result result = new getNextMoveValues_result();
        result.success = iface_.getNextMoveValues(args.gameName, args.configuration);
        oprot.writeMessageBegin(new TMessage("getNextMoveValues", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }

    }

    private class getMoveValue implements ProcessFunction {
      public void process(int seqid, TProtocol iprot, TProtocol oprot) throws TException
      {
        getMoveValue_args args = new getMoveValue_args();
        args.read(iprot);
        iprot.readMessageEnd();
        getMoveValue_result result = new getMoveValue_result();
        result.success = iface_.getMoveValue(args.gameName, args.configuration);
        oprot.writeMessageBegin(new TMessage("getMoveValue", TMessageType.REPLY, seqid));
        result.write(oprot);
        oprot.writeMessageEnd();
        oprot.getTransport().flush();
      }

    }

  }

  public static class getNextMoveValues_args implements TBase, java.io.Serializable, Cloneable, Comparable<getNextMoveValues_args>   {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final TStruct STRUCT_DESC = new TStruct("getNextMoveValues_args");
    private static final TField GAME_NAME_FIELD_DESC = new TField("gameName", TType.STRING, (short)-1);
    private static final TField CONFIGURATION_FIELD_DESC = new TField("configuration", TType.STRING, (short)-2);

    public String gameName;
    public String configuration;
    public static final int GAMENAME = -1;
    public static final int CONFIGURATION = -2;

    // isset id assignments

    public static final Map<Integer, FieldMetaData> metaDataMap = Collections.unmodifiableMap(new HashMap<Integer, FieldMetaData>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
      put(GAMENAME, new FieldMetaData("gameName", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRING)));
      put(CONFIGURATION, new FieldMetaData("configuration", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRING)));
    }});

    static {
      FieldMetaData.addStructMetaDataMap(getNextMoveValues_args.class, metaDataMap);
    }

    public getNextMoveValues_args() {
    }

    public getNextMoveValues_args(
      String gameName,
      String configuration)
    {
      this();
      this.gameName = gameName;
      this.configuration = configuration;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public getNextMoveValues_args(getNextMoveValues_args other) {
      if (other.isSetGameName()) {
        this.gameName = other.gameName;
      }
      if (other.isSetConfiguration()) {
        this.configuration = other.configuration;
      }
    }

    public getNextMoveValues_args deepCopy() {
      return new getNextMoveValues_args(this);
    }

    @Deprecated
    public getNextMoveValues_args clone() {
      return new getNextMoveValues_args(this);
    }

    public String getGameName() {
      return this.gameName;
    }

    public getNextMoveValues_args setGameName(String gameName) {
      this.gameName = gameName;
      return this;
    }

    public void unsetGameName() {
      this.gameName = null;
    }

    // Returns true if field gameName is set (has been asigned a value) and false otherwise
    public boolean isSetGameName() {
      return this.gameName != null;
    }

    public void setGameNameIsSet(boolean value) {
      if (!value) {
        this.gameName = null;
      }
    }

    public String getConfiguration() {
      return this.configuration;
    }

    public getNextMoveValues_args setConfiguration(String configuration) {
      this.configuration = configuration;
      return this;
    }

    public void unsetConfiguration() {
      this.configuration = null;
    }

    // Returns true if field configuration is set (has been asigned a value) and false otherwise
    public boolean isSetConfiguration() {
      return this.configuration != null;
    }

    public void setConfigurationIsSet(boolean value) {
      if (!value) {
        this.configuration = null;
      }
    }

    public void setFieldValue(int fieldID, Object value) {
      switch (fieldID) {
      case GAMENAME:
        if (value == null) {
          unsetGameName();
        } else {
          setGameName((String)value);
        }
        break;

      case CONFIGURATION:
        if (value == null) {
          unsetConfiguration();
        } else {
          setConfiguration((String)value);
        }
        break;

      default:
        throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
      }
    }

    public Object getFieldValue(int fieldID) {
      switch (fieldID) {
      case GAMENAME:
        return getGameName();

      case CONFIGURATION:
        return getConfiguration();

      default:
        throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
      }
    }

    // Returns true if field corresponding to fieldID is set (has been asigned a value) and false otherwise
    public boolean isSet(int fieldID) {
      switch (fieldID) {
      case GAMENAME:
        return isSetGameName();
      case CONFIGURATION:
        return isSetConfiguration();
      default:
        throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
      }
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof getNextMoveValues_args)
        return this.equals((getNextMoveValues_args)that);
      return false;
    }

    public boolean equals(getNextMoveValues_args that) {
      if (that == null)
        return false;

      boolean this_present_gameName = true && this.isSetGameName();
      boolean that_present_gameName = true && that.isSetGameName();
      if (this_present_gameName || that_present_gameName) {
        if (!(this_present_gameName && that_present_gameName))
          return false;
        if (!this.gameName.equals(that.gameName))
          return false;
      }

      boolean this_present_configuration = true && this.isSetConfiguration();
      boolean that_present_configuration = true && that.isSetConfiguration();
      if (this_present_configuration || that_present_configuration) {
        if (!(this_present_configuration && that_present_configuration))
          return false;
        if (!this.configuration.equals(that.configuration))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    public int compareTo(getNextMoveValues_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      getNextMoveValues_args typedOther = (getNextMoveValues_args)other;

      lastComparison = Boolean.valueOf(isSetGameName()).compareTo(isSetGameName());
      if (lastComparison != 0) {
        return lastComparison;
      }
      lastComparison = TBaseHelper.compareTo(gameName, typedOther.gameName);
      if (lastComparison != 0) {
        return lastComparison;
      }
      lastComparison = Boolean.valueOf(isSetConfiguration()).compareTo(isSetConfiguration());
      if (lastComparison != 0) {
        return lastComparison;
      }
      lastComparison = TBaseHelper.compareTo(configuration, typedOther.configuration);
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
          case GAMENAME:
            if (field.type == TType.STRING) {
              this.gameName = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case CONFIGURATION:
            if (field.type == TType.STRING) {
              this.configuration = iprot.readString();
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
      if (this.configuration != null) {
        oprot.writeFieldBegin(CONFIGURATION_FIELD_DESC);
        oprot.writeString(this.configuration);
        oprot.writeFieldEnd();
      }
      if (this.gameName != null) {
        oprot.writeFieldBegin(GAME_NAME_FIELD_DESC);
        oprot.writeString(this.gameName);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("getNextMoveValues_args(");
      boolean first = true;

      sb.append("gameName:");
      if (this.gameName == null) {
        sb.append("null");
      } else {
        sb.append(this.gameName);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("configuration:");
      if (this.configuration == null) {
        sb.append("null");
      } else {
        sb.append(this.configuration);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws TException {
      // check for required fields
      // check that fields of type enum have valid values
    }

  }

  public static class getNextMoveValues_result implements TBase, java.io.Serializable, Cloneable, Comparable<getNextMoveValues_result>   {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final TStruct STRUCT_DESC = new TStruct("getNextMoveValues_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRUCT, (short)0);

    public GetNextMoveResponse success;
    public static final int SUCCESS = 0;

    // isset id assignments

    public static final Map<Integer, FieldMetaData> metaDataMap = Collections.unmodifiableMap(new HashMap<Integer, FieldMetaData>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
      put(SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new StructMetaData(TType.STRUCT, GetNextMoveResponse.class)));
    }});

    static {
      FieldMetaData.addStructMetaDataMap(getNextMoveValues_result.class, metaDataMap);
    }

    public getNextMoveValues_result() {
    }

    public getNextMoveValues_result(
      GetNextMoveResponse success)
    {
      this();
      this.success = success;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public getNextMoveValues_result(getNextMoveValues_result other) {
      if (other.isSetSuccess()) {
        this.success = new GetNextMoveResponse(other.success);
      }
    }

    public getNextMoveValues_result deepCopy() {
      return new getNextMoveValues_result(this);
    }

    @Deprecated
    public getNextMoveValues_result clone() {
      return new getNextMoveValues_result(this);
    }

    public GetNextMoveResponse getSuccess() {
      return this.success;
    }

    public getNextMoveValues_result setSuccess(GetNextMoveResponse success) {
      this.success = success;
      return this;
    }

    public void unsetSuccess() {
      this.success = null;
    }

    // Returns true if field success is set (has been asigned a value) and false otherwise
    public boolean isSetSuccess() {
      return this.success != null;
    }

    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }

    public void setFieldValue(int fieldID, Object value) {
      switch (fieldID) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((GetNextMoveResponse)value);
        }
        break;

      default:
        throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
      }
    }

    public Object getFieldValue(int fieldID) {
      switch (fieldID) {
      case SUCCESS:
        return getSuccess();

      default:
        throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
      }
    }

    // Returns true if field corresponding to fieldID is set (has been asigned a value) and false otherwise
    public boolean isSet(int fieldID) {
      switch (fieldID) {
      case SUCCESS:
        return isSetSuccess();
      default:
        throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
      }
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof getNextMoveValues_result)
        return this.equals((getNextMoveValues_result)that);
      return false;
    }

    public boolean equals(getNextMoveValues_result that) {
      if (that == null)
        return false;

      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    public int compareTo(getNextMoveValues_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      getNextMoveValues_result typedOther = (getNextMoveValues_result)other;

      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      lastComparison = TBaseHelper.compareTo(success, typedOther.success);
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
          case SUCCESS:
            if (field.type == TType.STRUCT) {
              this.success = new GetNextMoveResponse();
              this.success.read(iprot);
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
      oprot.writeStructBegin(STRUCT_DESC);

      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        this.success.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("getNextMoveValues_result(");
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws TException {
      // check for required fields
      // check that fields of type enum have valid values
    }

  }

  public static class getMoveValue_args implements TBase, java.io.Serializable, Cloneable, Comparable<getMoveValue_args>   {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final TStruct STRUCT_DESC = new TStruct("getMoveValue_args");
    private static final TField GAME_NAME_FIELD_DESC = new TField("gameName", TType.STRING, (short)-1);
    private static final TField CONFIGURATION_FIELD_DESC = new TField("configuration", TType.STRING, (short)-2);

    public String gameName;
    public String configuration;
    public static final int GAMENAME = -1;
    public static final int CONFIGURATION = -2;

    // isset id assignments

    public static final Map<Integer, FieldMetaData> metaDataMap = Collections.unmodifiableMap(new HashMap<Integer, FieldMetaData>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
      put(GAMENAME, new FieldMetaData("gameName", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRING)));
      put(CONFIGURATION, new FieldMetaData("configuration", TFieldRequirementType.DEFAULT, 
          new FieldValueMetaData(TType.STRING)));
    }});

    static {
      FieldMetaData.addStructMetaDataMap(getMoveValue_args.class, metaDataMap);
    }

    public getMoveValue_args() {
    }

    public getMoveValue_args(
      String gameName,
      String configuration)
    {
      this();
      this.gameName = gameName;
      this.configuration = configuration;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public getMoveValue_args(getMoveValue_args other) {
      if (other.isSetGameName()) {
        this.gameName = other.gameName;
      }
      if (other.isSetConfiguration()) {
        this.configuration = other.configuration;
      }
    }

    public getMoveValue_args deepCopy() {
      return new getMoveValue_args(this);
    }

    @Deprecated
    public getMoveValue_args clone() {
      return new getMoveValue_args(this);
    }

    public String getGameName() {
      return this.gameName;
    }

    public getMoveValue_args setGameName(String gameName) {
      this.gameName = gameName;
      return this;
    }

    public void unsetGameName() {
      this.gameName = null;
    }

    // Returns true if field gameName is set (has been asigned a value) and false otherwise
    public boolean isSetGameName() {
      return this.gameName != null;
    }

    public void setGameNameIsSet(boolean value) {
      if (!value) {
        this.gameName = null;
      }
    }

    public String getConfiguration() {
      return this.configuration;
    }

    public getMoveValue_args setConfiguration(String configuration) {
      this.configuration = configuration;
      return this;
    }

    public void unsetConfiguration() {
      this.configuration = null;
    }

    // Returns true if field configuration is set (has been asigned a value) and false otherwise
    public boolean isSetConfiguration() {
      return this.configuration != null;
    }

    public void setConfigurationIsSet(boolean value) {
      if (!value) {
        this.configuration = null;
      }
    }

    public void setFieldValue(int fieldID, Object value) {
      switch (fieldID) {
      case GAMENAME:
        if (value == null) {
          unsetGameName();
        } else {
          setGameName((String)value);
        }
        break;

      case CONFIGURATION:
        if (value == null) {
          unsetConfiguration();
        } else {
          setConfiguration((String)value);
        }
        break;

      default:
        throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
      }
    }

    public Object getFieldValue(int fieldID) {
      switch (fieldID) {
      case GAMENAME:
        return getGameName();

      case CONFIGURATION:
        return getConfiguration();

      default:
        throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
      }
    }

    // Returns true if field corresponding to fieldID is set (has been asigned a value) and false otherwise
    public boolean isSet(int fieldID) {
      switch (fieldID) {
      case GAMENAME:
        return isSetGameName();
      case CONFIGURATION:
        return isSetConfiguration();
      default:
        throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
      }
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof getMoveValue_args)
        return this.equals((getMoveValue_args)that);
      return false;
    }

    public boolean equals(getMoveValue_args that) {
      if (that == null)
        return false;

      boolean this_present_gameName = true && this.isSetGameName();
      boolean that_present_gameName = true && that.isSetGameName();
      if (this_present_gameName || that_present_gameName) {
        if (!(this_present_gameName && that_present_gameName))
          return false;
        if (!this.gameName.equals(that.gameName))
          return false;
      }

      boolean this_present_configuration = true && this.isSetConfiguration();
      boolean that_present_configuration = true && that.isSetConfiguration();
      if (this_present_configuration || that_present_configuration) {
        if (!(this_present_configuration && that_present_configuration))
          return false;
        if (!this.configuration.equals(that.configuration))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    public int compareTo(getMoveValue_args other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      getMoveValue_args typedOther = (getMoveValue_args)other;

      lastComparison = Boolean.valueOf(isSetGameName()).compareTo(isSetGameName());
      if (lastComparison != 0) {
        return lastComparison;
      }
      lastComparison = TBaseHelper.compareTo(gameName, typedOther.gameName);
      if (lastComparison != 0) {
        return lastComparison;
      }
      lastComparison = Boolean.valueOf(isSetConfiguration()).compareTo(isSetConfiguration());
      if (lastComparison != 0) {
        return lastComparison;
      }
      lastComparison = TBaseHelper.compareTo(configuration, typedOther.configuration);
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
          case GAMENAME:
            if (field.type == TType.STRING) {
              this.gameName = iprot.readString();
            } else { 
              TProtocolUtil.skip(iprot, field.type);
            }
            break;
          case CONFIGURATION:
            if (field.type == TType.STRING) {
              this.configuration = iprot.readString();
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
      if (this.configuration != null) {
        oprot.writeFieldBegin(CONFIGURATION_FIELD_DESC);
        oprot.writeString(this.configuration);
        oprot.writeFieldEnd();
      }
      if (this.gameName != null) {
        oprot.writeFieldBegin(GAME_NAME_FIELD_DESC);
        oprot.writeString(this.gameName);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("getMoveValue_args(");
      boolean first = true;

      sb.append("gameName:");
      if (this.gameName == null) {
        sb.append("null");
      } else {
        sb.append(this.gameName);
      }
      first = false;
      if (!first) sb.append(", ");
      sb.append("configuration:");
      if (this.configuration == null) {
        sb.append("null");
      } else {
        sb.append(this.configuration);
      }
      first = false;
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws TException {
      // check for required fields
      // check that fields of type enum have valid values
    }

  }

  public static class getMoveValue_result implements TBase, java.io.Serializable, Cloneable, Comparable<getMoveValue_result>   {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final TStruct STRUCT_DESC = new TStruct("getMoveValue_result");
    private static final TField SUCCESS_FIELD_DESC = new TField("success", TType.STRUCT, (short)0);

    public GetMoveResponse success;
    public static final int SUCCESS = 0;

    // isset id assignments

    public static final Map<Integer, FieldMetaData> metaDataMap = Collections.unmodifiableMap(new HashMap<Integer, FieldMetaData>() {/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	{
      put(SUCCESS, new FieldMetaData("success", TFieldRequirementType.DEFAULT, 
          new StructMetaData(TType.STRUCT, GetMoveResponse.class)));
    }});

    static {
      FieldMetaData.addStructMetaDataMap(getMoveValue_result.class, metaDataMap);
    }

    public getMoveValue_result() {
    }

    public getMoveValue_result(
      GetMoveResponse success)
    {
      this();
      this.success = success;
    }

    /**
     * Performs a deep copy on <i>other</i>.
     */
    public getMoveValue_result(getMoveValue_result other) {
      if (other.isSetSuccess()) {
        this.success = new GetMoveResponse(other.success);
      }
    }

    public getMoveValue_result deepCopy() {
      return new getMoveValue_result(this);
    }

    @Deprecated
    public getMoveValue_result clone() {
      return new getMoveValue_result(this);
    }

    public GetMoveResponse getSuccess() {
      return this.success;
    }

    public getMoveValue_result setSuccess(GetMoveResponse success) {
      this.success = success;
      return this;
    }

    public void unsetSuccess() {
      this.success = null;
    }

    // Returns true if field success is set (has been asigned a value) and false otherwise
    public boolean isSetSuccess() {
      return this.success != null;
    }

    public void setSuccessIsSet(boolean value) {
      if (!value) {
        this.success = null;
      }
    }

    public void setFieldValue(int fieldID, Object value) {
      switch (fieldID) {
      case SUCCESS:
        if (value == null) {
          unsetSuccess();
        } else {
          setSuccess((GetMoveResponse)value);
        }
        break;

      default:
        throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
      }
    }

    public Object getFieldValue(int fieldID) {
      switch (fieldID) {
      case SUCCESS:
        return getSuccess();

      default:
        throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
      }
    }

    // Returns true if field corresponding to fieldID is set (has been asigned a value) and false otherwise
    public boolean isSet(int fieldID) {
      switch (fieldID) {
      case SUCCESS:
        return isSetSuccess();
      default:
        throw new IllegalArgumentException("Field " + fieldID + " doesn't exist!");
      }
    }

    @Override
    public boolean equals(Object that) {
      if (that == null)
        return false;
      if (that instanceof getMoveValue_result)
        return this.equals((getMoveValue_result)that);
      return false;
    }

    public boolean equals(getMoveValue_result that) {
      if (that == null)
        return false;

      boolean this_present_success = true && this.isSetSuccess();
      boolean that_present_success = true && that.isSetSuccess();
      if (this_present_success || that_present_success) {
        if (!(this_present_success && that_present_success))
          return false;
        if (!this.success.equals(that.success))
          return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return 0;
    }

    public int compareTo(getMoveValue_result other) {
      if (!getClass().equals(other.getClass())) {
        return getClass().getName().compareTo(other.getClass().getName());
      }

      int lastComparison = 0;
      getMoveValue_result typedOther = (getMoveValue_result)other;

      lastComparison = Boolean.valueOf(isSetSuccess()).compareTo(isSetSuccess());
      if (lastComparison != 0) {
        return lastComparison;
      }
      lastComparison = TBaseHelper.compareTo(success, typedOther.success);
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
          case SUCCESS:
            if (field.type == TType.STRUCT) {
              this.success = new GetMoveResponse();
              this.success.read(iprot);
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
      oprot.writeStructBegin(STRUCT_DESC);

      if (this.isSetSuccess()) {
        oprot.writeFieldBegin(SUCCESS_FIELD_DESC);
        this.success.write(oprot);
        oprot.writeFieldEnd();
      }
      oprot.writeFieldStop();
      oprot.writeStructEnd();
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder("getMoveValue_result(");
      sb.append("success:");
      if (this.success == null) {
        sb.append("null");
      } else {
        sb.append(this.success);
      }
      sb.append(")");
      return sb.toString();
    }

    public void validate() throws TException {
      // check for required fields
      // check that fields of type enum have valid values
    }

  }

}
