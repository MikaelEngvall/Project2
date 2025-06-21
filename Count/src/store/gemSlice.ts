import { createSlice, PayloadAction } from '@reduxjs/toolkit';

interface GemMine {
  id: number;
  gems: number;
  maxGems: number;
  productionRate: number;
  totalProduced: number;
}

interface GemState {
  mines: GemMine[];
  totalGems: number;
  timeElapsed: number;
  isRunning: boolean;
  speedMultiplier: number;
}

// Produktionstakt per sekund vid 60x hastighet
const PRODUCTION_RATE_AT_60X = 4.55373406193078; // 2.27686703096539 * 2

const initialState: GemState = {
  mines: [{
    id: 1,
    gems: 40000,
    maxGems: 40000,
    productionRate: PRODUCTION_RATE_AT_60X,
    totalProduced: 0,
  }],
  totalGems: 0,
  timeElapsed: 0,
  isRunning: false,
  speedMultiplier: 1,
};

const gemSlice = createSlice({
  name: 'gems',
  initialState,
  reducers: {
    updateMines: (state, action: PayloadAction<number>) => {
      const deltaTime = action.payload;
      state.timeElapsed += deltaTime;
      
      // Uppdatera varje gruva
      state.mines.forEach(mine => {
        // Skala produktionstakten med hastighetsmultiplikatorn
        const production = mine.productionRate * (state.speedMultiplier / 60) * deltaTime;
        // Bara uppdatera om gruvan inte är tom
        if (mine.gems > 0) {
          mine.gems = Math.max(0, mine.gems - production);
          mine.totalProduced += production;
          state.totalGems += production;
        }
      });

      // Köp nya gruvor när vi har tillräckligt med stenar
      while (state.totalGems >= 20000 && state.mines.length < 10) {
        state.totalGems -= 20000;
        state.mines.push({
          id: state.mines.length + 1,
          gems: 40000,
          maxGems: 40000,
          productionRate: PRODUCTION_RATE_AT_60X,
          totalProduced: 0,
        });
      }
    },
    toggleSimulation: (state) => {
      state.isRunning = !state.isRunning;
    },
    setSpeedMultiplier: (state, action: PayloadAction<number>) => {
      state.speedMultiplier = action.payload;
    },
    resetSimulation: (state) => {
      return initialState;
    },
  },
});

export const { updateMines, toggleSimulation, setSpeedMultiplier, resetSimulation } = gemSlice.actions;
export default gemSlice.reducer; 