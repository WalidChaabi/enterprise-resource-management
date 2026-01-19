const sharp = require('sharp');
const toIco = require('to-ico');
const fs = require('fs');
const path = require('path');

const svgPath = path.join(__dirname, '../src/assets/logos/logo-symbol-black.svg');
const outputPath = path.join(__dirname, '../src/favicon.ico');

async function generateFavicon() {
  try {
    console.log('Generating favicon.ico from logo-symbol-black.svg...');
    
    // Generate PNGs at different sizes for ICO
    const sizes = [16, 32, 48];
    const pngBuffers = [];
    
    for (const size of sizes) {
      const pngBuffer = await sharp(svgPath)
        .resize(size, size, {
          fit: 'contain',
          background: { r: 0, g: 0, b: 0, alpha: 0 }
        })
        .png()
        .toBuffer();
      
      pngBuffers.push(pngBuffer);
      console.log(`Generated ${size}x${size} PNG`);
    }
    
    // Convert PNGs to ICO
    const icoBuffer = await toIco(pngBuffers);
    
    // Write ICO file
    fs.writeFileSync(outputPath, icoBuffer);
    console.log(`✓ Favicon created successfully at ${outputPath}`);
  } catch (error) {
    console.error('Error generating favicon:', error);
    process.exit(1);
  }
}

generateFavicon();
