/**
 * Databasinställningar för MySQL
 */

module.exports = {
  host: 'localhost',
  port: 3306,
  user: 'root',      // Ändra till din MySQL-användare
  password: '1234',  // Ändra till ditt MySQL-lösenord
  database: 'fhs',   // Ändra till ditt databasnamn
  
  // För att hantera binära UUID korrekt
  supportBigNumbers: true,
  bigNumberStrings: true
}; 