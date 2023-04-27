const Ice = require('ice').Ice;
const Demo = require('./Servants').Demo;

async function main() {
    // const iceOptions = {
    //     properties: Ice.createProperties(),
    // };

    // // Set the Ice communicator properties
    // iceOptions.properties.setProperty('Ice.Default.Locator', 'Demo/Locator:default -p 10000');
    // iceOptions.properties.setProperty('Ice.Default.EncodingVersion', '1.0');
    // iceOptions.properties.setProperty('Ice.Trace.Network', '2');
    // iceOptions.properties.setProperty('Ice.Trace.Protocol', '2');
    // iceOptions.properties.setProperty('Ice.MessageSizeMax', '102400');

    const communicator = Ice.initialize();
    const base = communicator.stringToProxy("DemoAdapter:default -p 10000");
  
    const demoObject = Demo.DemoInterfacePrx.checkedCast(base);
    if (!demoObject) {
      throw new Error("Invalid proxy");
    }
  
    await demoObject.sayHello();
  
    communicator.destroy();
}

main()
    .then(() => console.log('Client finished'))
    .catch((error) => console.error(error));
