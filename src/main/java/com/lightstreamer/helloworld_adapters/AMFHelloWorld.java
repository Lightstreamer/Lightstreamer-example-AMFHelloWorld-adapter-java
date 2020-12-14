/*
 * Copyright (c) Lightstreamer Srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lightstreamer.helloworld_adapters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.lightstreamer.interfaces.data.DataProviderException;
import com.lightstreamer.interfaces.data.FailureException;
import com.lightstreamer.interfaces.data.ItemEventListener;
import com.lightstreamer.interfaces.data.SmartDataProvider;
import com.lightstreamer.interfaces.data.SubscriptionException;

import flex.messaging.io.SerializationContext;
import flex.messaging.io.amf.Amf3Output;


public class AMFHelloWorld implements SmartDataProvider {
  private ItemEventListener listener;
  private volatile GreetingsThread gt;
 
  public void init(Map params, File configDir) throws DataProviderException {
  }

  public boolean isSnapshotAvailable(String itemName) throws SubscriptionException {
    return false;
  }

  public void setListener(ItemEventListener listener) {
    this.listener = listener;
  }

  public void subscribe(String itemName, Object itemHandle, boolean needsIterator) 
        throws SubscriptionException, FailureException {
    if (itemName.equals("greetings")) {
      gt = new GreetingsThread(itemHandle);
      gt.start();
    }
  }
    
  public void subscribe(String itemName, boolean needsIterator)
        throws SubscriptionException, FailureException {
  }

  public void unsubscribe(String itemName) throws SubscriptionException,
        FailureException {
    if (itemName.equals("greetings") && gt != null) {
      gt.go = false;
    }
  }
  
  
  private static SerializationContext context = SerializationContext.getSerializationContext();
  
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

  class GreetingsThread extends Thread {

    private final Object itemHandle;
    
    public volatile boolean go = true;
   
    public GreetingsThread(Object itemHandle) {
      this.itemHandle = itemHandle;
    }
       
    public void run() {
      int c = 0;
      Random rand = new Random();
      HelloBean testBean = new HelloBean();
      BoolBean boolBean = new BoolBean();
      while(go) {
        Map<String,byte[]> data = new HashMap<String,byte[]>();
         
        testBean.setHello(c % 3 == 0 ? "Hello" : c % 3 == 1 ? "AMF" : "World");
        testBean.setNow(new Date());
                          
        data.put("AMF_field", toAMF(testBean));
        
        boolBean.setBool(!boolBean.getBool());
        
        data.put("AMF_bool", toAMF(boolBean));
        
        data.put("AMF_simple_bool", toAMF(!boolBean.getBool()));
       
        listener.smartUpdate(itemHandle, data, false);
        c++;
        try {
          Thread.sleep(1000 + rand.nextInt(2000));
        } catch (InterruptedException e) {
        }
      }
    }
   
  }
  
  public class BoolBean implements java.io.Serializable {

    private static final long serialVersionUID = 8089098202241893741L;
    
    private boolean bool;
    
    public BoolBean() {
    }
    
    public boolean getBool() {
        return this.bool;
    }
    
    public void setBool(boolean bool) {
        this.bool = bool;
    }
      
  }
  
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
}
