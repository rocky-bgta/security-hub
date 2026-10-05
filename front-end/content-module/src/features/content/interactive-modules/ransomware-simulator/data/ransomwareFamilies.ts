import { ExtensionValue } from '../types';

const pick = (options: Array<string>): string =>
  options[Math.floor(Math.random() * options.length)];

const randomChars = (length: number): string => {
  const chars =
    'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789';
  let result = '';
  for (let i = 0; i < length; i += 1) {
    result += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  return result;
};

const generateRandomAlphaNumeric = (): string => `.${randomChars(8)}`;

const generateBlackTurtleExtension = (): string => {
  const prefix = Math.random() > 0.5 ? 'HELLO' : 'HELP';
  const numDigits = Math.floor(Math.random() * 4) + 1;
  let numbers = '';
  for (let i = 0; i < numDigits; i += 1) {
    numbers += Math.floor(Math.random() * 10);
  }
  return `.${prefix}${numbers}`;
};

const generateAkoExtension = (): string =>
  pick(['.ako', '.locked', '.encrypted']);

const generateAvalancheExtension = (): string =>
  pick(['.encrypted', '.zepto', '.locky', '.odin', '.thor', '.zzzzz']);

const generateBartExtension = (): string => pick(['.bart', '.bart.zip']);

const generateBuranExtension = (): string =>
  pick(['.buran', '.buran@airmail.cc', '.BURAN', '.BURAN2']);

const generateChaosExtension = (): string => {
  if (Math.random() > 0.5) return '.encrypted';
  return `.${randomChars(4)}`;
};

const generateContiExtension = (): string => pick(['.conti', '.CONTI']);

const generateCrysisExtension = (): string =>
  pick(['.encrypted', '.ezz', '.zzz', '.vxk', '.ccc']);

const generateCryptoLockerExtension = (): string =>
  pick(['.encrypted', '.locked']);

const generateCryptoMixExtension = (): string =>
  pick(['.crypt', '.cryp1', '.crysis', '.rsa']);

const generateCryptoWallExtension = (): string =>
  pick(['.encrypted', '.ccc', '.aaa', '.vvv', '.xyz']);

const generateDarkSideExtension = (): string =>
  pick(['.darkside', '.dark']);

const generateDharmaExtension = (): string =>
  pick([
    '.cezar',
    '.arena',
    '.bip',
    '.cobra',
    '.crk',
    '.crypt',
    '.java',
    '.wallet',
    '.zzzzz',
  ]);

const generateDoppelPaymerExtension = (): string =>
  pick(['.encrypted', '.doppel']);

const generateDridexExtension = (): string =>
  pick(['.locky', '.odin', '.thor', '.zepto']);

const generateGandCrabExtension = (): string =>
  pick(['.GDCB', '.GRABB', '.CRAB', '.KRAB', '.encrypted']);

const generateGlobeExtension = (): string =>
  pick(['.globe', '.purge', '.oops', '.btc', '.toxcrypto']);

const generateGlobeImposterExtension = (): string =>
  pick(['.encrypted', '.doc', '.docx', '.xls', '.xlsx']);

const generateHermesExtension = (): string =>
  pick(['.HERMES', '.rsa2048']);

const generateHiveExtension = (): string =>
  pick(['.hive', '.key', '.txt']);

const generateJigsawExtension = (): string =>
  pick(['.jigsaw', '.fun', '.kkk']);

const generateJaffExtension = (): string => pick(['.jaff', '.jaffi']);

const generateLockBitExtension = (): string => {
  if (Math.random() > 0.7) return `.lockbit-${randomChars(6)}`;
  return pick(['.lockbit', '.lockbit2', '.lockbit3', '.locked']);
};

const generateLockyExtension = (): string =>
  pick(['.locky', '.odin', '.thor', '.zepto', '.aesir', '.lukitus']);

const generateMakopExtension = (): string => pick(['.makop', '.makop2']);

const generateMazeExtension = (): string => pick(['.maze', '.encrypted']);

const generateMedusaLockerExtension = (): string =>
  pick(['.medusa', '.encrypted', '.bck']);

const generateMespinozaExtension = (): string =>
  pick(['.mespinoza', '.encrypted']);

const generateNefilimExtension = (): string =>
  pick(['.nefilim', '.Nefilim']);

const generateNemtyExtension = (): string => pick(['.nemty', '.NEMTY']);

const generatePetyaExtension = (): string =>
  pick(['.encrypted', '.petya', '.petrwrap']);

const generatePhobosExtension = (): string =>
  pick([
    '.phobos',
    '.actin',
    '.adage',
    '.arena',
    '.bip',
    '.cobra',
    '.crk',
    '.java',
    '.wallet',
  ]);

const generateRagnarLockerExtension = (): string =>
  pick(['.ragnar', '.ragnar_locker']);

const generateRansomExxExtension = (): string =>
  pick(['.ransomexx', '.exx']);

const generateRyukExtension = (): string => pick(['.ryk', '.RYK']);

const generateScarabExtension = (): string =>
  pick(['.scarab', '.support', '.help']);

const generateSodinokibiExtension = (): string =>
  pick(['.encrypted', '.crypted', '.LOL!', '.Sodinokibi']);

const generateSporaExtension = (): string =>
  pick(['.spora', '.crypted', '.key']);

const generateStopExtension = (): string =>
  pick([
    '.stop',
    '.djvu',
    '.djvuu',
    '.udjvu',
    '.uudjvu',
    '.rumba',
    '.promorad',
    '.peta',
    '.peta2',
  ]);

const generateTroldeshExtension = (): string =>
  pick(['.crypted', '.encrypted', '.lol']);

const generateWannaCryExtension = (): string =>
  pick(['.WNCRY', '.WCRY', '.wcry']);

const generateWastedLockerExtension = (): string =>
  pick(['.wasted', '.wastedl0cker']);

const generateXoristExtension = (): string =>
  pick(['.xorist', '.encrypted', '.crypted', '.readme']);

const generateZeppelinExtension = (): string =>
  pick(['.zeppelin', '.zepp']);

export const RANSOMWARE_EXTENSIONS: Record<string, ExtensionValue> = {
  'Abyss Ransomware': '.ABYSS',
  'Akira Ransomware': '.akira',
  'Alpha Ransomware': generateRandomAlphaNumeric,
  'Ako Ransomware': generateAkoExtension,
  'Avalanche Ransomware': generateAvalancheExtension,
  'Avaddon Ransomware': '.avdn',
  'BadRabbit Ransomware': '',
  'Bart Ransomware': generateBartExtension,
  'Black Turtle Ransomware': generateBlackTurtleExtension,
  'BlackByte Ransomware': '.blackbyte',
  'BlackCat Ransomware': '.blackcat',
  'BlackMatter Ransomware': '.blackmatter',
  'BlackSuit Ransomware': '.hydra',
  'BO Team Ransomware': '.newbot',
  'Buran Ransomware': generateBuranExtension,
  'Chaos Ransomware': generateChaosExtension,
  'Clop Ransomware': '.clop',
  'Conti Ransomware': generateContiExtension,
  'Crysis Ransomware': generateCrysisExtension,
  'CryptoLocker Ransomware': generateCryptoLockerExtension,
  'CryptoMix Ransomware': generateCryptoMixExtension,
  'CryptoWall Ransomware': generateCryptoWallExtension,
  'DarkSide Ransomware': generateDarkSideExtension,
  'Dharma Ransomware': generateDharmaExtension,
  'DoppelPaymer Ransomware': generateDoppelPaymerExtension,
  'Dridex Ransomware': generateDridexExtension,
  'Egregor Ransomware': '.egregor',
  'Electronic Ransomware': '.ELCTRONIC',
  'EncrypTile Ransomware': '.encrytile',
  'Elibe Ransomware': '.elibe',
  'Everest Ransomware': '.everest',
  'Fancy Bear Ransomware': '',
  'FileCoder Ransomware': '',
  'Frosty Ransomware': '.frosty',
  'GandCrab Ransomware': generateGandCrabExtension,
  'Globe Ransomware': generateGlobeExtension,
  'GlobeImposter Ransomware': generateGlobeImposterExtension,
  'Haron Ransomware': '.haron',
  'Hermes Ransomware': generateHermesExtension,
  'HelloKitty Ransomware': '.hellokitty',
  'Hive Ransomware': generateHiveExtension,
  'Hydra Ransomware': '.hydra',
  'Jigsaw Ransomware': generateJigsawExtension,
  'Jaff Ransomware': generateJaffExtension,
  'Karma Ransomware': '.karma',
  'KeRanger Ransomware': '',
  'Lethal Lock Ransomware': '.lethal',
  'LockBit Ransomware': generateLockBitExtension,
  'Locky Ransomware': generateLockyExtension,
  'Lokas Ransomware': '.lokas',
  'Loki Ransomware': '.loki',
  'MAKOP Ransomware': generateMakopExtension,
  'Maze Ransomware': generateMazeExtension,
  'Matrix Ransomware': '.matrix',
  'MedusaLocker Ransomware': generateMedusaLockerExtension,
  'Megacortex Ransomware': '.megacortex',
  'Meow Ransomware': '.MEOW',
  'Mespinoza Ransomware': generateMespinozaExtension,
  'Mimic Ransomware': '.mimic',
  'Mole Ransomware': '.mole',
  'Nefilim Ransomware': generateNefilimExtension,
  'Nemty Ransomware': generateNemtyExtension,
  'New Live Team Ransomware': '.newlive.team',
  'New Ran Ransomware': '.lalo',
  'Night Crow Ransomware': '.nightcrow',
  'NoCry Ransomware': '.NoCry',
  'NoName Ransomware': '',
  'Onyx Ransomware': '.onyx',
  'OphionLocker Ransomware': '',
  'Petya Ransomware': generatePetyaExtension,
  'Phobos Ransomware': generatePhobosExtension,
  'Ping Ransomware': '.ping',
  'Prometheus Ransomware': '.prometheus',
  'PyLocky Ransomware': '.pylocky',
  'Quantum Ransomware': '.quantum',
  'RagnarLocker Ransomware': generateRagnarLockerExtension,
  'RansomExx Ransomware': generateRansomExxExtension,
  'Ryuk Ransomware': generateRyukExtension,
  'Salsa Ransomware': '.salsa',
  'SamSam Ransomware': '',
  'Scarab Ransomware': generateScarabExtension,
  'Schrodingercat Ransomware': '.schrodingercat',
  'SNet Ransomware': '.snet',
  'Snakes Ransomware': '.snakes',
  'Sodinokibi Ransomware': generateSodinokibiExtension,
  'Spora Ransomware': generateSporaExtension,
  'Stop Ransomware': generateStopExtension,
  'Tode Ransomware': '.tode',
  'Troldesh Ransomware': generateTroldeshExtension,
  'Tprc Ransomware': '.tprc',
  'Unkno Ransomware': '.unkno',
  'Venus Ransomware': '.venus',
  'VLocker Ransomware': '.vlocker',
  'WannaCry Ransomware': generateWannaCryExtension,
  'WastedLocker Ransomware': generateWastedLockerExtension,
  'WhisperGate Ransomware': '',
  'Xam Ransomware': '.xam',
  'Xorist Ransomware': generateXoristExtension,
  'Yanluowang Ransomware': '.yanluowang',
  'ZQ Ransomware': '.zq',
  'Zeppelin Ransomware': generateZeppelinExtension,
};

export const RANSOMWARE_FAMILY_NAMES = Object.keys(RANSOMWARE_EXTENSIONS);

export const resolveExtension = (family: string): string => {
  const value = RANSOMWARE_EXTENSIONS[family];
  if (typeof value === 'function') return value();
  return value ?? '.ransomware';
};
