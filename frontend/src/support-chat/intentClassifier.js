
//  Multinomial Naive Bayes intent classifier (frontend-only)

 

const INTENTS = [

  "STATEMENT",

  "TRANSFER",

  "DEPOSIT",

  "WITHDRAW",

  "ACCOUNT",

  "CUSTOMER",

  "SECURITY",

  "GENERAL",

];

 

const TRAIN = [

  //  STATEMENT

  ["show my statement", "STATEMENT"],

  ["bank statement", "STATEMENT"],

  ["download statement", "STATEMENT"],

  ["transaction history", "STATEMENT"],

  ["show transactions", "STATEMENT"],

  ["statement please", "STATEMENT"],

  ["fetch statement", "STATEMENT"],

 

  //  TRANSFER (more examples to avoid confusion)

  ["transfer money", "TRANSFER"],

  ["money transfer", "TRANSFER"],

  ["transfer amount", "TRANSFER"],

  ["how to transfer", "TRANSFER"],

  ["send money", "TRANSFER"],

  ["send funds", "TRANSFER"],

  ["move money", "TRANSFER"],

  ["transfer to another account", "TRANSFER"],

  ["transfer to account", "TRANSFER"],

  ["fund transfer", "TRANSFER"],

  ["transfer to friend", "TRANSFER"],

  ["transfer to bank account", "TRANSFER"],

 

  //  DEPOSIT

  ["deposit money", "DEPOSIT"],

  ["add money", "DEPOSIT"],

  ["how to deposit", "DEPOSIT"],

  ["make a deposit", "DEPOSIT"],

  ["deposit amount", "DEPOSIT"],

 

  //  WITHDRAW

  ["withdraw money", "WITHDRAW"],

  ["cash withdrawal", "WITHDRAW"],

  ["how to withdraw", "WITHDRAW"],

  ["withdraw amount", "WITHDRAW"],

 

  //  ACCOUNT (can be how-to OR sensitive request; ChatbotPanel decides)

  ["how to fetch my account details", "ACCOUNT"],

  ["how do i fetch my account details", "ACCOUNT"],

  ["how to view account details", "ACCOUNT"],

  ["how to check balance", "ACCOUNT"],

  ["where can i see my balance", "ACCOUNT"],

  ["account details", "ACCOUNT"],

  ["show my account", "ACCOUNT"],

  ["show my balance", "ACCOUNT"],

  ["what is my balance", "ACCOUNT"],

  ["check my account balance", "ACCOUNT"],

  ["balance", "ACCOUNT"],

  ["account balance", "ACCOUNT"],

  ["my balance", "ACCOUNT"],

 

  //  CUSTOMER (can be how-to OR sensitive request; ChatbotPanel decides)

  ["how to update profile", "CUSTOMER"],

  ["how to update kyc", "CUSTOMER"],

  ["how to check kyc status", "CUSTOMER"],

  ["customer details", "CUSTOMER"],

  ["my profile", "CUSTOMER"],

  ["update profile", "CUSTOMER"],

  ["update kyc", "CUSTOMER"],

  ["kyc status", "CUSTOMER"],

 

  //  SECURITY

  ["otp", "SECURITY"],

  ["pin", "SECURITY"],

  ["password", "SECURITY"],

  ["i forgot my password", "SECURITY"],

  ["reset password", "SECURITY"],

  ["forgot password", "SECURITY"],

 

  //  GENERAL

  ["help", "GENERAL"],

  ["support", "GENERAL"],

  ["what can you do", "GENERAL"],

  ["how does app work", "GENERAL"],

];

 

function tokenize(text) {

  return (text || "")

    .toLowerCase()

    .replace(/[^a-z0-9\s]/g, " ")

    .split(/\s+/)

    .filter(Boolean);

}

 

export function trainClassifier() {

  const vocab = new Map();

  const classDocCount = new Map();

  const classWordCount = new Map();

  const wordCounts = new Map();

  let totalDocs = 0;

 

  for (const intent of INTENTS) {

    classDocCount.set(intent, 0);

    classWordCount.set(intent, 0);

    wordCounts.set(intent, new Map());

  }

 

  for (const [text, label] of TRAIN) {

    totalDocs++;

    classDocCount.set(label, classDocCount.get(label) + 1);

 

    const words = tokenize(text);

    const wc = wordCounts.get(label);

 

    for (const w of words) {

      vocab.set(w, (vocab.get(w) || 0) + 1);

      wc.set(w, (wc.get(w) || 0) + 1);

      classWordCount.set(label, classWordCount.get(label) + 1);

    }

  }

 

  const vocabSize = vocab.size;

 

  function predict(text) {

    const words = tokenize(text);

    const scores = new Map();

 

    for (const intent of INTENTS) {

      const prior = Math.log(

        (classDocCount.get(intent) + 1) / (totalDocs + INTENTS.length)

      );

 

      let logp = prior;

      const wc = wordCounts.get(intent);

      const denom = classWordCount.get(intent) + vocabSize;

 

      for (const w of words) {

        const num = (wc.get(w) || 0) + 1; // Laplace smoothing

        logp += Math.log(num / denom);

      }

 

      scores.set(intent, logp);

    }

 

    let bestIntent = "GENERAL";

    let bestScore = -Infinity;

 

    for (const [k, v] of scores.entries()) {

      if (v > bestScore) {

        bestScore = v;

        bestIntent = k;

      }

    }

 

    const sorted = [...scores.values()].sort((a, b) => b - a);

    const margin = sorted.length >= 2 ? sorted[0] - sorted[1] : 0;

    const confidence = Math.max(0, Math.min(1, margin / 5));

 

    return { intent: bestIntent, confidence };

  }

 

  return { predict };

}
