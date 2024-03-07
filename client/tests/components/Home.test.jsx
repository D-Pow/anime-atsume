import Home from '@/components/Home';

import { renderWithWrappingParent } from '~/config/jest/jest.setup.libs';

describe('<Home/>', () => {
    it('should render the home page text', () => {
        const homeComponent = renderWithWrappingParent(<Home />);
        const homeTextComponent = homeComponent.getByText('Anime Atsume');

        expect(homeTextComponent).toBeDefined();
    });
});
