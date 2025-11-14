import { useState } from 'react';
import './App.css';

function App() {
  const [username, setUsername] = useState('');
  const [ratedFilms, setRatedFilms] = useState([]);
  const [loading, setLoading] = useState(false);
  const [loadingPosters, setLoadingPosters] = useState(false);
  const [error, setError] = useState(null);
  const [gameState, setGameState] = useState('input'); // 'input', 'playing', 'gameover'
  const [currentFilm, setCurrentFilm] = useState(null);
  const [nextFilm, setNextFilm] = useState(null);
  const [streak, setStreak] = useState(0);
  const [revealed, setRevealed] = useState(false);

  const fetchFilms = async () => {
    if (!username.trim()) {
      setError('Please enter a username');
      return;
    }

    setLoading(true);
    setError(null);
    try {
      const response = await fetch(`/api/films/${username}`);
      if (!response.ok) {
        throw new Error('Failed to fetch films. Check the username.');
      }
      const data = await response.json();

      // Filter to only films with ratings
      const rated = data.filter(film => film.rating !== null);

      if (rated.length < 2) {
        throw new Error('Not enough rated films to play. Need at least 2.');
      }

      setRatedFilms(rated);
      startGame(rated);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const getRandomFilm = (filmsArray, excludeFilm = null) => {
    let film;
    do {
      film = filmsArray[Math.floor(Math.random() * filmsArray.length)];
    } while (excludeFilm && film.title === excludeFilm.title);
    return film;
  };

  const fetchPostersForPair = async (film1, film2) => {
    try {
      const response = await fetch('/api/films/posters', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify([film1, film2]),
      });

      if (!response.ok) {
        throw new Error('Failed to fetch posters');
      }

      const filmsWithPosters = await response.json();

      // If we got back less than 2 films, one or both don't have posters
      if (filmsWithPosters.length < 2) {
        return null; // Signal to try different films
      }

      return filmsWithPosters;
    } catch (err) {
      console.error('Error fetching posters:', err);
      return null;
    }
  };

  const startGame = async (rated) => {
    setLoadingPosters(true);
    let filmsWithPosters = null;
    let attempts = 0;

    // Try up to 10 times to find two films with posters
    while (!filmsWithPosters && attempts < 10) {
      const film1 = rated[Math.floor(Math.random() * rated.length)];
      const film2 = getRandomFilm(rated, film1);

      filmsWithPosters = await fetchPostersForPair(film1, film2);
      attempts++;
    }

    setLoadingPosters(false);

    if (!filmsWithPosters) {
      setError('Could not find films with posters. Try a different username.');
      setGameState('input');
      return;
    }

    setCurrentFilm(filmsWithPosters[0]);
    setNextFilm(filmsWithPosters[1]);
    setStreak(0);
    setRevealed(false);
    setGameState('playing');
  };

  const loadNextRound = async () => {
    setLoadingPosters(true);
    let filmsWithPosters = null;
    let attempts = 0;

    while (!filmsWithPosters && attempts < 10) {
      const newNextFilm = getRandomFilm(ratedFilms, nextFilm);
      filmsWithPosters = await fetchPostersForPair(nextFilm, newNextFilm);
      attempts++;
    }

    setLoadingPosters(false);

    if (!filmsWithPosters) {
      setError('Could not load next round');
      setGameState('gameover');
      return;
    }

    setCurrentFilm(filmsWithPosters[0]); // The previous nextFilm
    setNextFilm(filmsWithPosters[1]); // New film
    setRevealed(false);
  };

  const handleGuess = (guess) => {
    if (revealed) return;

    const currentRating = currentFilm.rating;
    const nextRating = nextFilm.rating;

    let correct = false;
    if (guess === 'higher' && nextRating > currentRating) correct = true;
    if (guess === 'same' && nextRating === currentRating) correct = true;
    if (guess === 'lower' && nextRating < currentRating) correct = true;

    setRevealed(true);

    if (correct) {
      setStreak(prev => prev + 1);
      setTimeout(() => {
        loadNextRound();
      }, 1500);
    } else {
      setTimeout(() => {
        setGameState('gameover');
      }, 1500);
    }
  };

  const resetGame = () => {
    setUsername('');
    setRatedFilms([]);
    setGameState('input');
    setStreak(0);
    setError(null);
  };

  const playAgain = () => {
    startGame(ratedFilms);
  };

  if (gameState === 'input') {
    return (
      <div className="App">
        <header className="App-header fade-in">
          <h1 className="game-title">Letterboxd Clash</h1>
          <p style={{ fontSize: '18px', marginBottom: '20px', opacity: 0.8 }}>
            Guess if the next film has a higher, same, or lower rating!
          </p>

          <div className="input-container">
            <input
              type="text"
              className="username-input"
              placeholder="Enter Letterboxd username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && fetchFilms()}
              disabled={loading}
            />
            <button
              className="game-button button-primary"
              onClick={fetchFilms}
              disabled={loading}
            >
              {loading ? 'Loading Films...' : 'Start Game'}
            </button>
          </div>

          {error && (
            <div className="error-message fade-in">
              {error}
            </div>
          )}
        </header>
      </div>
    );
  }

  if (gameState === 'gameover') {
    return (
      <div className="App">
        <header className="App-header fade-in">
          <h1 className="game-title">Game Over!</h1>
          <div className="game-over-score">
            Streak: {streak}
          </div>

          <div className="button-container">
            <button
              className="game-button button-primary"
              onClick={playAgain}
            >
              Play Again
            </button>
            <button
              className="game-button button-secondary"
              onClick={resetGame}
            >
              Change Username
            </button>
          </div>
        </header>
      </div>
    );
  }

  if (loadingPosters) {
    return (
      <div className="App">
        <header className="App-header">
          <h1 className="game-title">Letterboxd Clash</h1>
          <div className="streak-counter">
            Streak: {streak}
          </div>
          <div className="loading-text">
            Loading posters...
          </div>
        </header>
      </div>
    );
  }

  return (
    <div className="App">
      <header className="App-header">
        <h1 className="game-title">Letterboxd Clash</h1>
        <div className="streak-counter">
          Streak: {streak}
        </div>

        <div className="film-container">
          {/* Current Film - Rating Visible */}
          <div className="film-card fade-in">
            <img
              src={currentFilm.image}
              alt={currentFilm.title}
              className="film-poster"
            />
            <h3 className="film-title">
              {currentFilm.title}
            </h3>
            <div className="rating-stars">
              {'★'.repeat(Math.floor(currentFilm.rating))}
              {currentFilm.rating % 1 !== 0 && '½'}
            </div>
            <div className="rating-number">
              {currentFilm.rating} / 5
            </div>
          </div>

          <div className="vs-text">VS</div>

          {/* Next Film - Rating Hidden Until Revealed */}
          <div className="film-card fade-in">
            <img
              src={nextFilm.image}
              alt={nextFilm.title}
              className="film-poster"
            />
            <h3 className="film-title">
              {nextFilm.title}
            </h3>
            {revealed ? (
              <div className="rating-reveal">
                <div className="rating-stars">
                  {'★'.repeat(Math.floor(nextFilm.rating))}
                  {nextFilm.rating % 1 !== 0 && '½'}
                </div>
                <div className="rating-number">
                  {nextFilm.rating} / 5
                </div>
              </div>
            ) : (
              <div className="rating-stars" style={{ color: '#999' }}>???</div>
            )}
          </div>
        </div>

        {!revealed && (
          <div className="button-container fade-in">
            <button
              className="game-button button-higher"
              onClick={() => handleGuess('higher')}
            >
              Higher
            </button>
            <button
              className="game-button button-same"
              onClick={() => handleGuess('same')}
            >
              Same
            </button>
            <button
              className="game-button button-lower"
              onClick={() => handleGuess('lower')}
            >
              Lower
            </button>
          </div>
        )}
      </header>
    </div>
  );
}

export default App;
