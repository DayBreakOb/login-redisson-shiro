var randomMax = 100000000;
var index = 1;
var numberOfLetter = 26;
var numberIndex = 48;
var numberMax = 9;
var capitalIndex = 65;
var lowerCaseIndex = 97;
var special = 0;
var specialCharacter = [[33, 14], [58, 6], [91, 5], [123, 3]];
var length = 16;
var strengthLevel = 4;

function StringBuffer() {

    this.__strings__ = [];

}

StringBuffer.prototype.Append = function (str) {
    this.__strings__.push(str);
    return this;
}

StringBuffer.prototype.toString = function () {

    return this.__strings__.join('');
}
StringBuffer.prototype.size = function () {
    return this.__strings__.length;
}
StringBuffer.prototype.clear = function () {
    this.__strings__ = [];
}

function getRandom() {
    var sb = new StringBuffer();
    if (length <= 0)
        throw 'length can not <=0';
    for (var i = 0; i < length; i++) {
        var xx = getNextChar();
        sb.Append(String.fromCodePoint(xx))
    }
    return sb.toString();
}

function getRandom1(strengthLevel, length) {

    this.strengthLevel = strengthLevel;
    this.length = length;
    return getRandom();
}


function getNextChar() {
    if (strengthLevel < 1 || strengthLevel > 4)
        throw 'this level is not suppported';
    //ascii
    var x = 0;
    //伪字符ascii
    var puppetLetter = Math.ceil(Math.random() * randomMax) % numberOfLetter;
    //伪数字ascii
    var pupperNumber = Math.ceil(Math.random() * randomMax) % numberIndex +numberIndex;

    var levelIndex = Math.ceil(Math.random() * randomMax) % strengthLevel;
    //特殊字符数组一维
    var specialType = Math.ceil(Math.random() * randomMax) % specialCharacter.length;
    //特殊字符二维ascii
    var specialInt = Math.ceil(Math.random() * randomMax) % specialCharacter[specialType][index] + specialCharacter[specialType][special];

    switch (strengthLevel) {

        case 1:
            x = pupperNumber;
            break;
        case 2:
            if (levelIndex == index)
                x = pupperNumber;
            else
                x = puppetLetter + lowerCaseIndex;
            break;
        case 3:

            if (levelIndex == 0)
                x = pupperNumber;
            else if (levelIndex == index)
                x = puppetLetter + lowerCaseIndex;
            else x = puppetLetter + capitalIndex;
            break;
        case 4:
            if (levelIndex == 0)
                x = pupperNumber;
            else if (levelIndex == index)
                x = puppetLetter + lowerCaseIndex;
            else if (levelIndex == index * 2)
                x = puppetLetter + capitalIndex;
            else x = specialInt;
            break;
        default:
            break;
    }


    return x;
}



function getAesString(data, key, iv) {
	const key1 = CryptoJS.enc.Utf8.parse(key);
	const iv1 = CryptoJS.enc.Utf8.parse(iv);
	const encrypted = CryptoJS.AES.encrypt(data, key1, {
		iv : iv1,
		mode : CryptoJS.mode.CBC,
		padding : CryptoJS.pad.Pkcs7
	});
	return encrypted.toString();
}

function getAES(data) {
	var timestamp=new Date().getTime();
	var key = getRandom1(4,19)+timestamp;
	var iv = getRandom1(4,3)+timestamp;
	const encrypted = getAesString(data, key, iv);
	const encrypted1 = CryptoJS.enc.Utf8.parse(encrypted);
	const crykey= getRsaEncry(key);
	const cryiv = getRsaEncry(iv);
	return encrypted+"|"+crykey+"|"+cryiv;
}


