import { configureStore } from '@reduxjs/toolkit';
import gemReducer from './gemSlice';

export const store = configureStore({
  reducer: {
    gems: gemReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch; 