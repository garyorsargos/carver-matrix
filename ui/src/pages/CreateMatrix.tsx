import { Box, FormControl, InputLabel, MenuItem, Select, Typography } from "@mui/material";
export const CreateMatrix: React.FC = () => {
  return (
    // This Box is for the whole screen (background); where every other box will be built on.
    <Box
      sx={{
        backgroundColor: 'lightgray',
        height: '100vh',
        width: '100vw',
        display: 'flex',
        justifyContent: 'left',
        alignItems: 'flex-start',
        p: 2
      }}
    >
      {/* This Box is the Left-Half of the screen and it deals with filling the Matrix Parameters and Global Category Multipliers*/}
      <Box
        id='matrixCreatorBox'
        sx={{
          backgroundColor: 'white',
          p: 2,
          borderRadius: 2,
          width: '50%',
          height: '85%',
          boxShadow: 3,
          position: 'center',
          top: '10px',
          display: 'flex',
          alignItems: 'flex-start',
          flexDirection: 'column'
        }}
      >
        <Typography variant="h2">Example Matrix Title</Typography>
        <Typography variant="body1">Example Matrix Description...</Typography>
        
        {/* Space of 50px (INEFFICIENT, CONSIDER USING STACK COMPONENT INSTEAD) */}
        <Box sx={{ height: '50px' }}></Box>
        
        <Typography variant="h4">Matrix Parameters</Typography>

        {/* Space of 50px (INEFFICIENT, CONSIDER USING STACK COMPONENT INSTEAD) */}
        <Box sx={{ height: '50px' }}></Box>

        {/* This Box is for the Matrix Parameters */}
        <Box
        id='matrixParametersBox'
        sx={{
          backgroundColor: 'white',
          width: '100%',
          height: '100%',
          position: 'center',
          display: 'flex',
          alignItems: 'flex-start',
          flexDirection: 'column'
        }}
        >

          {/* Input Label Box */}
          <InputLabel id="score-range-label">Score Range</InputLabel>
          <FormControl fullWidth sx={{ border: '1px solid lightgray', borderRadius: '50px'}}>
              <Select
                labelId="score-range-label"
                id="score-range-select"
                value={0}
                label="Score Range"
                variant="outlined"
                sx={{ 
                  borderRadius: '50px', 
                  '& .MuiOutlinedInput-notchedOutline': {
                    borderRadius: '50px',
                  }
                }}
              >
              <MenuItem value={10}>Ten</MenuItem>
              <MenuItem value={20}>Twenty</MenuItem>
              <MenuItem value={30}>Thirty</MenuItem>
            </Select>
          </FormControl>
          
          {/* Input Label Box */}
          <InputLabel id="score-range-label">Data Entry Assignment Method</InputLabel>
          <FormControl fullWidth sx={{ border: '1px solid lightgray', borderRadius: '50px'}}>
              <Select
                labelId="demo-simple-select-label"
                id="demo-simple-select"
                value={0}
                label="Method"
                variant="outlined"
                sx={{ 
                  borderRadius: '50px', 
                  '& .MuiOutlinedInput-notchedOutline': {
                    borderRadius: '50px',
                  }
                }}
              >
              <MenuItem value={10}>Ten</MenuItem>
              <MenuItem value={20}>Twenty</MenuItem>
              <MenuItem value={30}>Thirty</MenuItem>
            </Select>
          </FormControl>
        </Box>

        {/* This Box is for the Global Category Multipliers */}
        <Box
        id='GlobalCategoryMultipliersBox'
        sx={{
          backgroundColor: 'white',
          width: '50%',
          height: '85%',
          position: 'center',
          display: 'flex',
          alignItems: 'flex-start',
          flexDirection: 'column'
        }}
        >
          {/* TO DO: implement CARVER Global Category Multipliers (i.e., 1.2x Criticality, 0.8x Recognizability, etc) */}
        </Box>
      </Box>

      {/* Space between the Boxes */}
      <Box sx={{ width: '20px' }} /> 

      {/* This Box is the Right-Half of the screen and it deals with managing participants */}
      <Box
        id='manageParticipantsBox'
        sx={{
          backgroundColor: 'white',
          p: 2,
          borderRadius: 2,
          width: '50%',
          height: '85%',
          boxShadow: 3,
          position: 'center',
          top: '10px',
          display: 'flex',
          alignItems: 'flex-start',
          flexDirection: 'column'
        }}
      >
        Work in progress...
      </Box>
    </Box>
  );
};

export default CreateMatrix;