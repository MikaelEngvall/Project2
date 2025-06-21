import React, { useEffect, useRef } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { RootState } from './store/store';
import { updateMines, toggleSimulation, setSpeedMultiplier, resetSimulation } from './store/gemSlice';

function App() {
  const dispatch = useDispatch();
  const { mines, totalGems, timeElapsed, isRunning, speedMultiplier } = useSelector((state: RootState) => state.gems);
  const lastUpdate = useRef(Date.now());

  useEffect(() => {
    let animationFrameId: number;

    const update = () => {
      const now = Date.now();
      const deltaTime = (now - lastUpdate.current) / 1000; // Convert to seconds
      lastUpdate.current = now;

      if (isRunning) {
        dispatch(updateMines(deltaTime * speedMultiplier));
      }

      animationFrameId = requestAnimationFrame(update);
    };

    animationFrameId = requestAnimationFrame(update);
    return () => cancelAnimationFrame(animationFrameId);
  }, [dispatch, isRunning, speedMultiplier]);

  const formatTime = (seconds: number) => {
    const months = Math.floor(seconds / (30 * 24 * 60 * 60));
    const days = Math.floor((seconds % (30 * 24 * 60 * 60)) / (24 * 60 * 60));
    return `${months} månader ${days} dagar`;
  };

  return (
    <div className="min-h-screen bg-gray-100 py-8 px-4">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-center mb-8">Ädelstensgruvor Simulator</h1>
        
        <div className="bg-white rounded-lg shadow-lg p-6 mb-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-4">
            <div>
              <p className="text-lg font-semibold">Totalt antal ädelstenar: {Math.round(totalGems)}</p>
              <p className="text-lg font-semibold">Tid som gått: {formatTime(timeElapsed)}</p>
            </div>
            <div className="flex flex-col gap-2">
              <button
                onClick={() => dispatch(toggleSimulation())}
                className={`px-4 py-2 rounded ${
                  isRunning ? 'bg-red-500 hover:bg-red-600' : 'bg-green-500 hover:bg-green-600'
                } text-white`}
              >
                {isRunning ? 'Pausa' : 'Starta'} Simulering
              </button>
              <button
                onClick={() => dispatch(resetSimulation())}
                className="px-4 py-2 rounded bg-gray-500 hover:bg-gray-600 text-white"
              >
                Återställ
              </button>
            </div>
          </div>

          <div className="mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Hastighetsmultiplikator: {speedMultiplier}x
            </label>
            <input
              type="range"
              min="1"
              max="60"
              value={speedMultiplier}
              onChange={(e) => dispatch(setSpeedMultiplier(Number(e.target.value)))}
              className="w-full"
            />
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-lg p-6">
          <h2 className="text-xl font-semibold mb-4">Gruvor</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {mines.map((mine) => (
              <div key={mine.id} className="border rounded-lg p-4">
                <h3 className="font-semibold mb-2">Gruva #{mine.id}</h3>
                <div className="w-full bg-gray-200 rounded-full h-2.5 mb-2">
                  <div
                    className="bg-blue-600 h-2.5 rounded-full"
                    style={{ width: `${(mine.gems / mine.maxGems) * 100}%` }}
                  ></div>
                </div>
                <p className="text-sm">
                  {Math.round(mine.gems)} / {mine.maxGems} ädelstenar
                </p>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default App; 