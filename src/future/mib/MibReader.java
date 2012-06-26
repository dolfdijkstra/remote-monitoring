package com.fatwire.management.snmp.mib;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import net.percederberg.mibble.Mib;
import net.percederberg.mibble.MibLoader;
import net.percederberg.mibble.MibLoaderException;
import net.percederberg.mibble.MibMacroSymbol;
import net.percederberg.mibble.MibSymbol;
import net.percederberg.mibble.MibType;
import net.percederberg.mibble.MibTypeSymbol;
import net.percederberg.mibble.MibTypeTag;
import net.percederberg.mibble.MibValue;
import net.percederberg.mibble.MibValueSymbol;
import net.percederberg.mibble.snmp.SnmpAgentCapabilities;
import net.percederberg.mibble.snmp.SnmpModuleCompliance;
import net.percederberg.mibble.snmp.SnmpModuleIdentity;
import net.percederberg.mibble.snmp.SnmpNotificationGroup;
import net.percederberg.mibble.snmp.SnmpNotificationType;
import net.percederberg.mibble.snmp.SnmpObjectGroup;
import net.percederberg.mibble.snmp.SnmpObjectIdentity;
import net.percederberg.mibble.snmp.SnmpObjectType;
import net.percederberg.mibble.snmp.SnmpTextualConvention;
import net.percederberg.mibble.snmp.SnmpTrapType;
import net.percederberg.mibble.snmp.SnmpType;
import net.percederberg.mibble.type.BitSetType;
import net.percederberg.mibble.type.BooleanType;
import net.percederberg.mibble.type.ChoiceType;
import net.percederberg.mibble.type.ElementType;
import net.percederberg.mibble.type.IntegerType;
import net.percederberg.mibble.type.NullType;
import net.percederberg.mibble.type.ObjectIdentifierType;
import net.percederberg.mibble.type.RealType;
import net.percederberg.mibble.type.SequenceOfType;
import net.percederberg.mibble.type.SequenceType;
import net.percederberg.mibble.type.StringType;
import net.percederberg.mibble.type.TypeReference;
import net.percederberg.mibble.value.BitSetValue;
import net.percederberg.mibble.value.BooleanValue;
import net.percederberg.mibble.value.NullValue;
import net.percederberg.mibble.value.NumberValue;
import net.percederberg.mibble.value.ObjectIdentifierValue;
import net.percederberg.mibble.value.StringValue;
import net.percederberg.mibble.value.ValueReference;

public class MibReader {
    MibLoader loader = new MibLoader();

    public Mib loadMib(File file) throws MibLoaderException, IOException {
        loader.addDir(file.getParentFile());
        return loader.load(file);
    }

    public Mib loadMib(String name) throws MibLoaderException, IOException {

        //loader.addDir(file.getParentFile());
        return loader.load(name);
    }

    public HashMap extractOids(Mib mib) {
        HashMap map = new HashMap();
        System.out.println(mib.getName());
        //System.out.println(mib.getFile());
        //extractOid(mib.getRootSymbol());

        Iterator iter = mib.getAllSymbols().iterator();

        while (iter.hasNext()) {
            MibSymbol symbol = (MibSymbol) iter.next();
            //System.out.println("symbol name: " + symbol.getName());

            //System.out.println(symbol.getClass());
            ObjectIdentifierValue value = extractOid(symbol);
            if (value != null) {
                map.put(symbol.getName(), value);
                //System.out.println("object: "+value.toObject());
                //System.out.println("name: "+);
                //System.out.println("detailString: "+value.toDetailString());
                print(value);
                /*
                        */

            }
        }
        return map;
    }

    void print(ObjectIdentifierValue value) {

        System.out.println(value.getName() + " " + value.toObject() + " "
                + value.toDetailString());

    }

    public ObjectIdentifierValue extractOid(MibSymbol symbol) {

        //System.out.println(symbol.toString());
        //System.out.println(symbol.getClass());

        if (symbol instanceof MibValueSymbol) {
            //System.out.println("symbol name: " + symbol.getName());
            MibValueSymbol mvs = ((MibValueSymbol) symbol);
            MibType type = mvs.getType();
            printType(type);
            MibValue v = mvs.getValue();

            //System.out.println("value name: " + v.getName());
            //System.out.println(value.getClass());
            if (v instanceof BitSetValue) {
                BitSetValue value = (BitSetValue) v;
            } else if (v instanceof BooleanValue) {
                BooleanValue value = (BooleanValue) v;
            } else if (v instanceof NullValue) {
                NullValue value = (NullValue) v;
            } else if (v instanceof NumberValue) {
                NumberValue value = (NumberValue) v;
            } else if (v instanceof ObjectIdentifierValue) {
                ObjectIdentifierValue value = (ObjectIdentifierValue) v;
                return value;
            } else if (v instanceof StringValue) {
                StringValue value = (StringValue) v;
            } else if (v instanceof ValueReference) {
                ValueReference value = (ValueReference) v;
            } else {
                System.out.println("unknown value : " + v.getClass());
            }
        } else if (symbol instanceof MibTypeSymbol) {
            //System.out.println("symbol name: " + symbol.getName());
            MibTypeSymbol mts = (MibTypeSymbol) symbol;

            //System.out.println("mts: " + mts.toString());
            MibType type = mts.getType();
            //System.out.println("type: " + type.getClass());
            //System.out.println("type comment: " + type.getComment());

            //System.out.println("type: " + type.toString());
            //System.out.println("type tag: " + type.getTag());
            printType(type);
        } else if (symbol instanceof MibMacroSymbol) {
            MibMacroSymbol mms = ((MibMacroSymbol) symbol);
            System.out.println("macro: " + mms.toString());

        } else {
            System.out.println("### symbol class: " + symbol.getClass());
        }

        return null;
    }

    void printMibTypeTag(MibTypeTag mit) {
        if(true)return;
        if (mit == null)
            return;
        System.out.println(mit.getCategory());
        printMibTypeTag(mit.getNext());

    }

    private void printType(MibType type) {
        if (true) return;
        System.out.println("type: " + type.getName());
        System.out.println("type primitive: " + type.isPrimitive());
        System.out.println("type tag: " + type.getTag());
        System.out.println("type: " + type.getClass());
        System.out.println("type: " + type);

        printMibTypeTag(type.getTag());

        if (type instanceof BitSetType) {
            BitSetType value = (BitSetType) type;
        } else if (type instanceof BooleanType) {
            BooleanType value = (BooleanType) type;
        } else if (type instanceof ChoiceType) {
            ChoiceType value = (ChoiceType) type;
        } else if (type instanceof ElementType) {
            ElementType value = (ElementType) type;
        } else if (type instanceof IntegerType) {
            IntegerType value = (IntegerType) type;
        } else if (type instanceof NullType) {
            NullType value = (NullType) type;
        } else if (type instanceof ObjectIdentifierType) {
            ObjectIdentifierType value = (ObjectIdentifierType) type;
        } else if (type instanceof RealType) {
            RealType value = (RealType) type;
        } else if (type instanceof SequenceOfType) {
            SequenceOfType value = (SequenceOfType) type;
        } else if (type instanceof SequenceType) {
            SequenceType value = (SequenceType) type;
        } else if (type instanceof StringType) {
            StringType value = (StringType) type;

        } else if (type instanceof TypeReference) {
            TypeReference value = (TypeReference) type;
        } else if (type instanceof SnmpAgentCapabilities) {
            SnmpAgentCapabilities value = (SnmpAgentCapabilities) type;
        } else if (type instanceof SnmpModuleCompliance) {
            SnmpModuleCompliance value = (SnmpModuleCompliance) type;
        } else if (type instanceof SnmpModuleIdentity) {
            SnmpModuleIdentity value = (SnmpModuleIdentity) type;
        } else if (type instanceof SnmpNotificationGroup) {
            SnmpNotificationGroup value = (SnmpNotificationGroup) type;
        } else if (type instanceof SnmpNotificationType) {
            SnmpNotificationType value = (SnmpNotificationType) type;
        } else if (type instanceof SnmpObjectGroup) {
            SnmpObjectGroup value = (SnmpObjectGroup) type;

        } else if (type instanceof SnmpObjectIdentity) {
            SnmpObjectIdentity value = (SnmpObjectIdentity) type;
        } else if (type instanceof SnmpObjectType) {
            SnmpObjectType value = (SnmpObjectType) type;
            MibType syntax = value.getSyntax();

            System.out.println(syntax);
            printType(syntax);
        } else if (type instanceof SnmpTextualConvention) {
            SnmpTextualConvention value = (SnmpTextualConvention) type;
        } else if (type instanceof SnmpTrapType) {
            SnmpTrapType value = (SnmpTrapType) type;
        } else if (type instanceof SnmpType) {
            //types above prefixed with Snmp are all SnmpTypes
            SnmpType value = (SnmpType) type;
        }
    }

    public static void main(String[] a) {
        MibReader reader = new MibReader();
        try {
            reader.extractOids(reader.loadMib("IF-MIB"));
        } catch (MibLoaderException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

}
