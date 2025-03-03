const express = require('express');
const app = express();
const finnhub = require('finnhub');
const cors = require('cors');
const axios = require('axios');
const { MongoClient } = require('mongodb');

app.use(express.json());

const FINNHUB_API_KEY = 'cncn0hpr01qkavtmoiv0cncn0hpr01qkavtmoivg';
const POLYGON_API_KEY = '7po3YX1zXfgmIrKPijsWSeJpQzywBk8l';

// Enable CORS middleware
app.use(cors());

const path = require('path');

app.use(express.static(path.join(__dirname, 'dist/frontend')));

app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, 'dist/frontend/index.html'));
});



// Define route to handle stock search
app.post('/search', async (req, res) => {
    console.log('Post request');
  const { symbol } = req.body; // Get the ticker symbol from the query parameter
    console.log('Symbol:', symbol);
    const company_profile = await axios.get(`https://finnhub.io/api/v1/stock/profile2?symbol=${symbol}&token=${FINNHUB_API_KEY}`);
    const company_profile_data = company_profile.data;
    // res.setHeader('Access-Control-Allow-Origin', 'http://localhost:50272');
    res.json(company_profile_data);
});

app.get('/autocomplete', async (req, res) => {
  try {
    console.log('autocomplete route');
    const { q } = req.query;
    console.log('Query:', q);
    const url = `https://finnhub.io/api/v1/search?q=${q}&from=2022-01-01&token=${FINNHUB_API_KEY}`;
    const response = await axios.get(url);
    res.json(response.data);
  } catch (error) {
    console.error('Error fetching autocomplete suggestions:', error);
    res.status(500).send('Error fetching autocomplete suggestions');
  }
});

app.get('/peers', async (req,res) => {
  try {
    console.log('peers route');
    const { q: symbol } = req.query; // Renamed `q` to `symbol`
    if (!symbol) {
      throw new Error('Symbol parameter is missing');
    }
    console.log('Query displayed from peers: ', symbol);
    const url = `https://finnhub.io/api/v1/stock/peers?symbol=${symbol}&token=${FINNHUB_API_KEY}`;
    const response = await axios.get(url);
    res.json(response.data);
  } catch (error) {
    console.error('Error fetching Peers data: ', error);
    res.status(500).send('Error fetching Peers');
  }
})

app.get('/recommendation', async (req,res) => {
  try {
    console.log('recommendation route');
    const {q : symbol} = req.query;
    if (!symbol) {
      throw new Error('Symbol parameter is missing');
    }
    console.log('Query displayed from recommendation: ', symbol);
    const url = `https://finnhub.io/api/v1/stock/recommendation?symbol=${symbol}&token=${FINNHUB_API_KEY}`
    const response = await axios.get(url);
    res.json(response.data);
  }
  catch (error){
    console.error('Error fetching recommendation data: ', error);
    res.status(500).send('Error fetching recommendation');
  }
})

app.get('/historical', async (req,res) => {
  try {
    console.log('historical route');
    const {q: symbol} = req.query;
    if (!symbol) {
      throw new Error ('Symbol parameter is missing');
    }
    console.log('Query displayed from historical: ', symbol);
    const today = new Date();
    const twoYearsAgo = new Date(today.getFullYear() - 2, today.getMonth(), today.getDate());
    const from_month = String(twoYearsAgo.getMonth() + 1).padStart(2, '0');
    const from_date = `${twoYearsAgo.getFullYear()}-${from_month}-${String(twoYearsAgo.getDate()).padStart(2, '0')}`;
    const to_month = String(today.getMonth() + 1).padStart(2, '0');
    const to_date = `${today.getFullYear()}-${to_month}-${String(today.getDate()).padStart(2, '0')}`;
    console.log('From date and to date: ',from_date, to_date)
    const url = `https://api.polygon.io/v2/aggs/ticker/${symbol}/range/1/day/${from_date}/${to_date}?adjusted=true&sort=asc&apiKey=${POLYGON_API_KEY}`
    const response = await axios.get(url);
    res.json(response.data.results);
  } catch (error) {
    console.error('Error fetching historical data: ', error);
    res.status(500).send('Error fetching historical');
  }
})

app.get('/hourly', async (req,res) => {
  try {
    console.log('hourly route');
    const { symbol } = req.query;
    console.log('Query displayed from hourly: ', symbol);
    if (!symbol) {
      throw new Error ('Symbol parameter is missing');
    }
    console.log('Query displayed from hourly: ', symbol);

    const quoteResponse = await axios.get(`https://finnhub.io/api/v1/quote?symbol=${symbol}&token=${FINNHUB_API_KEY}`);
    const quoteData = quoteResponse.data;
    const marketCloseTimestamp = quoteData.t;
    const marketCloseDate = new Date(marketCloseTimestamp * 1000);

    const formattedMarketCloseTimestamp = `${marketCloseDate.getFullYear()}-${(marketCloseDate.getMonth() + 1).toString().padStart(2, '0')}-${marketCloseDate.getDate().toString().padStart(2, '0')} ${marketCloseDate.getHours().toString().padStart(2, '0')}:${marketCloseDate.getMinutes().toString().padStart(2, '0')}:${marketCloseDate.getSeconds().toString().padStart(2, '0')}`;
    const formattedMarketCloseDate = formattedMarketCloseTimestamp.split(' ')[0];
    const currentTimestamp = Math.floor(new Date().getTime() / 1000);
    const currentDate = new Date(currentTimestamp * 1000);

    const formattedCurrentTimestamp = `${currentDate.getFullYear()}-${(currentDate.getMonth() + 1).toString().padStart(2, '0')}-${currentDate.getDate().toString().padStart(2, '0')} ${currentDate.getHours().toString().padStart(2, '0')}:${currentDate.getMinutes().toString().padStart(2, '0')}:${currentDate.getSeconds().toString().padStart(2, '0')}`;
    const marketOpen = currentTimestamp - marketCloseTimestamp < 300;

    let hourlyFromDate, hourlyToDate;

 if (marketOpen) {
  hourlyToDate = new Date().toISOString().split('T')[0];
  const hourlyToDateObj = new Date(hourlyToDate);
  const hourlyFromDateObj = new Date(hourlyToDateObj);
  hourlyFromDateObj.setDate(hourlyToDateObj.getDate() - 1);
  hourlyFromDate = hourlyFromDateObj.toISOString().split('T')[0];
} else {
  hourlyToDate = formattedMarketCloseDate;
  const hourlyToDateObj = new Date(hourlyToDate);
  const hourlyFromDateObj = new Date(hourlyToDateObj);
  
  hourlyFromDateObj.setDate(hourlyToDateObj.getDate() - 1);
  hourlyFromDate = hourlyFromDateObj.toISOString().split('T')[0];
}
console.log(hourlyFromDate, hourlyToDate);
    const url = `https://api.polygon.io/v2/aggs/ticker/${symbol}/range/1/hour/${hourlyFromDate}/${hourlyToDate}?adjusted=true&sort=asc&apiKey=${POLYGON_API_KEY}`;
    // const url = `https://api.polygon.io/v2/aggs/ticker/${symbol}/range/1/hour/${hourlyFromDate}/${hourlyToDate}?adjusted=true&sort=asc&apiKey=${POLYGON_API_KEY}`
    const response = await axios.get(url);
    res.json(response.data.results);
  }
  catch (error) {
    console.error('Error fetching hourly data: ', error);
    res.status(500).send('Error fetching hourly');
  }
})

app.get('/earnings', async (req,res) => {
  try {
    console.log('earnings');
    const {q: symbol} = req.query;
    if (!symbol) {
      throw new Error ('Symbol parameter is missing');
    }
    console.log('Query displayed from earnings: ', symbol);
    const url = `https://finnhub.io/api/v1/stock/earnings?symbol=${symbol}&token=${FINNHUB_API_KEY}`;
    const response = await axios.get(url);
    res.json(response.data);
  }
  catch(error) {
    console.error('Error fetching earnings data: ', error);
    res.status(500).send('Error fetching earning');
  }
})

app.get('/insider-sentiment', async (req,res) => {
  try {
    console.log('sentiment route');
    const {q: symbol} = req.query;
    if (!symbol) {
      throw new Error('Symbol parameter is missing');
    }
    console.log('Query displayed from sentiment: ', symbol);
    const url = `https://finnhub.io/api/v1/stock/insider-sentiment?symbol=${symbol}&from=2022-01-01&token=${FINNHUB_API_KEY}`
    const response = await axios.get(url);
    res.json(response.data.data);
  }
  catch (error) {
    console.error('Error fetching sentiment data: ', error);
    res.status(500).send('Error fetching sentiment');
  }
})

app.get('/news', async (req,res) => {
  try{
    console.log('news route');
    const {q: symbol} = req.query;
    if (!symbol) {
      throw new Error('Symbol parameter is missing');
    }
    const currentDate = new Date();

// Calculate from_date as 1 week before the current date
const from_date = new Date(currentDate);
from_date.setDate(currentDate.getDate() - 7);

// Format from_date as YYYY-MM-DD
const from_date_formatted = from_date.toISOString().slice(0, 10);

// Format current date as YYYY-MM-DD
const to_date_formatted = currentDate.toISOString().slice(0, 10);

console.log("from_date:", from_date_formatted);
console.log("to_date:", to_date_formatted);

    console.log('Query displayed from news: ', symbol);
    const url = `https://finnhub.io/api/v1/company-news?symbol=${symbol}&from=${from_date_formatted}&to=${to_date_formatted}&token=${FINNHUB_API_KEY}`
    const response = await axios.get(url);
    res.json(response.data);
  }
  catch (error) {
    console.error('Error fetching news data: ', error);
    res.status(500).send('Error fetching news');
  }
})

app.get('/quoteDetails', async (req, res) => {
  try {
    const { symbol } = req.query;
    const url = `https://finnhub.io/api/v1/quote?symbol=${symbol}&token=${FINNHUB_API_KEY}`;
    const response = await axios.get(url);
    // console.log('From quoteDetails: ',res.json(response.data));
    res.json(response.data);
  } catch (error) {
    console.error('Error fetching quote details:', error);
    res.status(500).send('Error fetching quote details');
  }
});

const uri = "mongodb+srv://venkatna:ash1304@cluster0.0ljk6o2.mongodb.net/?retryWrites=true&w=majority";
const client = new MongoClient(uri);

async function connectToDatabase() {
  try {
    await client.connect();
    console.log("Connected to MongoDB Atlas");
  } catch (err) {
    console.error("Error connecting to MongoDB Atlas:", err);
  }
}

// Call the connectToDatabase function to establish the connection
connectToDatabase();

// Access your database and collection
const db = client.db('HW3');
const watchlistCollection = db.collection('watchlist');

app.post('/add-to-watchlist', (req, res) => {
  const { symbol, companyName, lastPrice, change, percentChange } = req.body;
  console.log(req.body);

  // Check if the symbol already exists in the watchlist
  watchlistCollection.findOne({ symbol })
    .then(existingDocument => {
      if (existingDocument) {
        // Symbol exists, update the existing document
        return watchlistCollection.updateOne({ symbol }, {
          $set: { companyName, lastPrice, change, percentChange }
        });
      } else {
        // Symbol does not exist, insert a new document
        return watchlistCollection.insertOne({ symbol, companyName, lastPrice, change, percentChange });
      }
    })
    .then(() => {
      console.log(`Added/Updated ${symbol} in the watchlist`);
      // res.sendStatus(200); // Send success status
    })
    .catch(error => {
      console.error('Error adding/updating to watchlist:', error);
      res.sendStatus(500); // Send error status
    });
});


app.get('/watchlist', async (req, res) => {
  try {
    const watchlistData = await watchlistCollection.find().toArray();
    res.json(watchlistData);
  } catch (error) {
    console.error('Error retrieving watchlist data:', error);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});

app.post('/remove-from-watchlist', async (req,res) => {
    const symbol = req.body.symbol;
    console.log('From removeWatch: ', symbol);
    watchlistCollection.deleteOne({ symbol: symbol }, (err, result) => {
      if (err) {
        console.error('Error removing from watchlist:', err);
        res.status(500).send('Error removing from watchlist');
      } else {
        console.log(`Removed ${symbolToRemove} from watchlist`);
        console.log('Successfully removed from watchlist');
      }
    });
});

const walletCollection = db.collection('wallet');

// walletCollection.deleteMany({});

// const balance = 25000;
// walletCollection.insertOne({ balance }).then(result => {
//   console.log('Document inserted successfully:', result);
// })
// .catch(error => {
//   console.error('Error inserting document:', error);
// });

app.get('/balance', (req,res) => {
  walletCollection.findOne({})
  .then(doc => {
    res.json(doc);
  })
  .catch(error => {
    console.error('Error fetching balance data:', error);
    res.status(500).json({ error: 'Internal Server Error' });
  });
});

app.post('/update-wallet', async (req, res) => {
  try {
    const { newBalance } = req.body;
    await walletCollection.updateOne({}, { $set: { balance: newBalance } });
    res.status(200).json({ message: 'Wallet updated successfully' });
  } catch (error) {
    console.error('Error updating wallet:', error);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});

const portfolioCollection = db.collection('portfolio');

app.post('/add-to-portfolio', async (req, res) => {
  try {
    const { symbol, companyName, quantity, total, currentPrice } = req.body;

    // Check if the symbol already exists in the portfolio
    const existingPortfolioItem = await portfolioCollection.findOne({ symbol });

    if (existingPortfolioItem) {
      // If the symbol exists, update the existing document
      const updatedQuantity = existingPortfolioItem.quantity + quantity;
      const updatedTotal = existingPortfolioItem.total + total;
      const updatedAverageCost = updatedTotal / updatedQuantity;

      // Update the document in the portfolio collection
      await portfolioCollection.updateOne(
        { symbol },
        {
          $set: {
            companyName,
            quantity: updatedQuantity,
            total: updatedTotal,
            averageCost: updatedAverageCost,
            currentPrice
          }
        }
      );
    } else {
      // If the symbol doesn't exist, insert a new document
      await portfolioCollection.insertOne({
        symbol,
        companyName,
        quantity,
        total,
        averageCost: total / quantity,
        currentPrice
      });
    }

    res.status(200).json({ success: true });
  } catch (error) {
    console.error('Error adding to portfolio:', error);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});

app.post('/sell-from-portfolio', async (req, res) => {
  try {
    const { symbol, companyName, quantity, currentPrice} = req.body;

    // Retrieve the portfolio item for the specified symbol
    const portfolioItem = await portfolioCollection.findOne({ symbol });

    if (!portfolioItem) {
      return res.status(404).json({ error: 'Portfolio item not found' });
    }

    if (portfolioItem.quantity < quantity) {
      return res.status(400).json({ error: 'Insufficient quantity in portfolio' });
    }

    const newQuantity = portfolioItem.quantity - quantity;
    const newTotal = portfolioItem.total - quantity*currentPrice;

    if (newQuantity <= 0) {
      await portfolioCollection.deleteOne({ symbol });
    } else {
      await portfolioCollection.updateOne(
        { symbol },
        {
          $set: {
            quantity: newQuantity,
            total: portfolioItem.total - newTotal,
            companyName: companyName,
            averageCost: newTotal/newQuantity,
            currentPrice: currentPrice
          }
        }
      );
    }
    res.status(200).json({ success: true });
  } catch (error) {
    console.error('Error selling from portfolio:', error);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});



app.get('/portfolio', async (req, res) => {
  try {
    // Retrieve portfolio data from MongoDB
    const portfolioData = await portfolioCollection.find().toArray();
    res.json(portfolioData);
  } catch (error) {
    console.error('Error retrieving portfolio data:', error);
    res.status(500).json({ error: 'Internal Server Error' });
  }
});

// Start the server
const PORT = 8080;
app.listen(PORT, () => {
  console.log(`Server is listening on port ${PORT}`);
});


