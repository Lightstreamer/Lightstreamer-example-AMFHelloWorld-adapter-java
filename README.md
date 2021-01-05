# Lightstreamer - "Hello World" Tutorial - Java SE (AMF) Adapter #
<!-- START DESCRIPTION lightstreamer-example-amfhelloworld-adapter-java -->

This demo, of the "Hello World with Lightstreamer" series, will focus on a new feature that was [released](http://cometdaily.com/2010/02/22/lightstreamer-36-released/) with [Lightstreamer Server](http://www.lightstreamer.com/download.htm) since version 3.6: <b>Action Message Format (AMF)</b> support for Flex applications.

This project shows the Data Adapter and Metadata Adapters for the *Hello World" Tutorial* to show how to use AMF with the Lightstreamer Flex Client Library ([docs](https://lightstreamer.com/api/ls-flex-client/latest/index.html)).

As an example of a client using this adapter, you may refer to the ["Hello World" Tutorial - Flex (AMF) Client](https://github.com/Lightstreamer/Lightstreamer-example-AMFHelloWorld-client-flex).

## Details

First, a quick recap of the previous installments:

- "Hello World" with Lightstreamer: An introduction to Lightstreamer's data model, [Lightstreamer - "Hello World" Tutorial - HTML Client](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-client-javascript), and [Lightstreamer - "Hello World" Tutorial - Java Adapter](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-adapter-java).
- [Lightstreamer - "Hello World" Tutorial - .NET Adapter](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-adapter-dotnet): The .NET API version of the Data Adapter used in the "Hello World" application, showing both a C# and a Visual Basic port.
- [Lightstreamer - "Hello World" Tutorial - TCP Sockets Adapter](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-adapter-socket): The TCP-socket-based version of the Data Adapter, suitable for implementation in other languages (PHP, Python, Perl, etc).

Basically, Lightstreamer Server can be seen as a "technology hub" for data push, where you can mix different technologies on the client-side and on the server-side to exchange real-time messages.<br>
![Schema](technology-hub1.png)

We will delve into the "Flex on the client-side, Java on the server-side" scenario and, in this project, full details for the server-side will be provided.<br>

As you may recall from the first installment [Lightstreamer- "Hello World" Tutorial - HTTP Client](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-client-javascript), Lightstreamer data push model is based on items made up of fields. A client can subscribe to many items, each with its own schema (a set of fields). Each field is usually a text string with an arbitrary length (from a few characters to large data structures, perhaps based on XML, JSON, etc.). With this new Lightstreamer feature, you can now put a full AMF object into any field and have it pushed from the server to the client in real binary format (with no re-encodings).<br>

The Flex Client library for Lightstreamer has been used for one of the <b>major dealing platforms</b> in the finance field and has undergone many cycles of improvements to make it completely production resilient. We were asked to add native support for AMF objects to improve the performance when streaming complex data structures. So now you can push both text-based items and object-based items to the same Flex application.<br>

When approaching the Lightstreamer <b>data model</b>, it is important to choose the right trade-off between using <i>fine-grained fields and coarse objects</i>. You could map each individual atomic piece of information to a Lightstreamer field, thus using many fields and items, or you could map all your data to a single field of a single item. This applies to both text-based fields (where you can encode coarse objects via JSON, XML, etc.) and object-based fields (via AMF). Usually, going for fine-grained fields is better, because you let Lightstreamer know more about your data structure, so that it will be able to apply optimization mechanisms, like conflation and delta delivery. On the other hand, if you go for huge opaque objects, Lightstreamer will be used more as a blind pipe. But in both cases, you will still benefit from other features, like bandwidth allocation and dynamic throttling. All intermediate scenarios are possible, too.<br>

In the sections below, it will be used a single field containing an AMF object derived from a JavaBean. To encode a JavaBean as an AMF object, several ready-made libraries exist. Here, we will leverage <b>BlazeDS</b>.<br>

### AMF Lightstreamer Tutorial

This project focuses on a simple "Hello World" example to show how to use AMF with our new Flex client library ([docs](https://lightstreamer.com/api/ls-flex-client/latest/index.html)). We will create a JavaBean on the server-side and then use it on the client-side.

For this tutorial, I'm assuming you have already read the ["Hello World" Tutorial - Java Adapter](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-adapter-java) and ["Hello World" Tutorial - HTML Client](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-client-javascript) basic examples, or that you are already familiar with Lightstreamer concepts.

On the client, the result of this tutorial will be quite similar to the one obtained with the original ["Hello World" Tutorial - HTML Client](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-client-javascript), but in Flex: we'll get a string alternating some different values (Hello AMF World) and a timestamp. On the server-side, data will be encapsulated into a <b>JavaBean</b> containing a String and a Date instance. This bean will be translated into a byte array and then injected into the Lightstreamer kernel as a single field, instead of being spread over different fields as simple strings (as the original adapter does). Here lies the power of AMF, as you will be able to push even complex JavaBeans to your Flex clients with ease.

<!-- END DESCRIPTION lightstreamer-example-amfhelloworld-adapter-java -->

### Dig the Code

This adapter reuses most of the code of the first Hello World. We add a static property and a static utility method to the class we've renamed into "AMFHelloWorld".

```java
private static SerializationContext context = 
  SerializationContext.getSerializationContext();
 
public static byte[] toAMF(Object bean) { 
  ByteArrayOutputStream baos = new ByteArrayOutputStream();
  Amf3Output output = new Amf3Output(context);
  output.setOutputStream(baos);
  try {
    output.writeObject(bean);
    output.flush();
    output.close();
  } catch (IOException e1) {
    e1.printStackTrace();
  }
  return baos.toByteArray();
}
```
The conversion from Java beans to an AMF-compatible byte array is performed by a couple of the [BlazeDS libraries](https://sourceforge.net/adobe/blazeds/wiki/Home/). 
The <i>toAMF</i> method receives an Object instance and converts it into an AMF byte array using the <i>Amf3Output</i> class. You can find a list of the conversions performed to switch from Java to AMF in the [ActionMessageOutput class javadoc](http://livedocs.adobe.com/blazeds/1/javadoc/flex/messaging/io/amf/ActionMessageOutput.html#writeObject(java.lang.Object).<br>

In this case, we're going to use a Java bean. Note that the <i>AMF3Output class</i> javadoc is currently not linked/listed on [BlazeDS classes list](https://livedocs.adobe.com/blazeds/1/javadoc/overview-tree.html) (they forgot?). You can reach it anyway, at the [logical directory](http://livedocs.adobe.com/blazeds/1/javadoc/flex/messaging/io/amf/Amf3Output.html).

Once the conversion method is in place, we can add the bean we want to send to the clients. We will prepare a simple bean containing only two properties, a String and a Date:

```java
public class HelloBean implements java.io.Serializable {
 
  private static final long serialVersionUID = 7965747352089964767L;
  private String hello;
  private Date now;
 
  public HelloBean() {
  }
 
  public String getHello() {
    return hello;
  }
 
  public void setHello(String hello) {
    this.hello = hello;
  }
 
  public Date getNow() {
    return now;
  }
 
  public void setNow(Date now) {
    this.now = now;
  }
}
```

Finally, we replace the run method of the <i>GreetingThread</i> inner class with a different implementation that handles our bean:

```java
public void run() {
  int c = 0;
  Random rand = new Random();
  HelloBean testBean = new HelloBean();
  while(go) {
    Map<String,byte[]> data = new HashMap<String,byte[]>();
 
    testBean.setHello(c % 3 == 0 ? "Hello" : c % 3 == 1 ? "AMF" : "World");
    testBean.setNow(new Date());
 
    data.put("AMF_field", toAMF(testBean));
 
    listener.smartUpdate(itemHandle, data, false);
    c++;
    try {
        Thread.sleep(1000 + rand.nextInt(2000));
    } catch (InterruptedException e) {
    }
  }
}
```

As you can see, there's nothing extremely complicated here, we just convert the instance of our bean through the <i>toAMF</i> method and inject it in a Map as if it was a simple String. Then, in turn, the Map is injected into the Lightstreamer kernel to make its way to the clients.

Please find the complete Java Adapter source in the "AMFHelloWorld.java" source file of this project.

In the `adapters.xml` configuration file, we use "AMFHELLOWORLD" as the adapter set name, as expected by the [Lightstreamer - "Hello World Tutorial - Flex (AMF) Client"](https://github.com/Lightstreamer/Lightstreamer-example-AMFHelloWorld-client-flex), while we use the classic [LiteralBasedProvider](https://github.com/Lightstreamer/Lightstreamer-lib-adapter-java-inprocess#literalbasedprovider-metadata-adapter), already provided by Lightstreamer server, as our Metadata Adapter and our AMFHelloWorld class as our Data Adapter.<br>
The `adapters.xml` file under the `LS_HOME/adapters/AMFHelloWorld` folder looks like:
```xml
<?xml version="1.0"?>
 
<adapters_conf id="AMFHELLOWORLD">
  <metadata_provider>
    <adapter_class>
      com.lightstreamer.adapters.metadata.LiteralBasedProvider
    </adapter_class>
  </metadata_provider>
  <data_provider>
    <adapter_class>AMFHelloWorld</adapter_class>
  </data_provider>
</adapters_conf>
```

<i>NOTE: not all configuration options of an Adapter Set are exposed by the file suggested above. 
You can easily expand your configurations using the generic template, see the [Java In-Process Adapter Interface Project](https://github.com/Lightstreamer/Lightstreamer-lib-adapter-java-inprocess#configuration) for details.</i><br>
<br>
Please refer [here](https://lightstreamer.com/docs/ls-server/latest/General%20Concepts.pdf) for more details about Lightstreamer Adapters.


### Notes

You've seen how to push Objects instead of Strings from a Lightstreamer server to a Flex client. You can exploit this technique to push complex data structures, but obviously, in doing so, you'll lose some of the optimizations offered by Lightstreamer protocol. For example, the merging algorithm (of the MERGE mode) is applied to the entire bean, instead of being applied to each single field, so that every time a property within the bean changes, the entire bean is pushed to the client, not only the changed value. As with anything regarding engineering, you'll have to choose the trade-off that optimizes properly for your application.

## Install

If you want to install a version of this demo in your local Lightstreamer Server, follow these steps:
* Download *Lightstreamer Server* (Lightstreamer Server comes with a free non-expiring demo license for 20 connected users) from [Lightstreamer Download page](https://lightstreamer.com/download/), and install it, as explained in the `GETTING_STARTED.TXT` file in the installation home directory.
* Get the `deploy.zip` file of the [latest release](https://github.com/Lightstreamer/Lightstreamer-example-AMFHelloWorld-adapter-java/releases), unzip it, and copy the just unzipped `AMFHelloWorld` folder into the `adapters` folder of your Lightstreamer Server installation.
* Launch Lightstreamer Server.
* Launch a client like the ["Hello World" Tutorial - Flex (AMF) Client](https://github.com/Lightstreamer/Lightstreamer-example-AMFHelloWorld-client-flex).

## Build

To build your own version of `example-amfhelloworld-adapter-java-0.0.1-SNAPSHOT.jar`, instead of using the one provided in the `deploy.zip` file from the [Install](https://github.com/Lightstreamer/Lightstreamer-example-AMFHelloWorld-adapter-java#install) section above, you have two options:
either use [Maven](https://maven.apache.org/) (or other build tools) to take care of dependencies and building (recommended) or gather the necessary jars yourself and build it manually.

For the sake of simplicity only the Maven case is detailed here.

### Maven

You can easily build and run this application using Maven through the pom.xml file located in the root folder of this project. As an alternative, you can use an alternative build tool (e.g. Gradle, Ivy, etc.) by converting the provided pom.xml file.

Assuming Maven is installed and available in your path you can build the demo by running
```sh 
 mvn install dependency:copy-dependencies 
```

## See Also

### Clients Using This Adapter
<!-- START RELATED_ENTRIES -->

* [Lightstreamer - "Hello World" Tutorial - Flex (AMF) Client](https://github.com/Lightstreamer/Lightstreamer-example-AMFHelloWorld-client-flex)

<!-- END RELATED_ENTRIES -->

### Related Projects

* [Lightstreamer - "Hello World" Tutorial - HTML Client](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-client-javascript)
* [Lightstreamer - "Hello World" Tutorial - Java Adapter](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-adapter-java)
* [Lightstreamer - "Hello World" Tutorial - .NET Adapter](https://github.com/Lightstreamer/Lightstreamer-example-HelloWorld-adapter-dotnet)
* [LiteralBasedProvider Metadata Adapter](https://github.com/Lightstreamer/Lightstreamer-lib-adapter-java-inprocess#literalbasedprovider-metadata-adapter)

## Lightstreamer Compatibility Notes 

- Compatible with Lightstreamer Flex Client Library version 2.0 or newer.
- Compatible with Lightstreamer SDK for Java In-Process Adapters since 7.3.
- For a version of this example compatible with Lightstreamer SDK for Java Adapters version 6.0, please refer to [this tag](https://github.com/Lightstreamer/Lightstreamer-example-AMFHelloWorld-adapter-java/tree/pre_mvn).
- For a version of this example compatible with Lightstreamer SDK for Java Adapters version 5.1, please refer to [this tag](https://github.com/Lightstreamer/Lightstreamer-example-AMFHelloWorld-adapter-java/tree/for_Lightstreamer_5.1).
<br>
- Ensure that Flex Client API is supported by Lightstreamer Server license configuration.
